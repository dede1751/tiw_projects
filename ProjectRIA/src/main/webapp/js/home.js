/**
 * Home Page Management
 */
(function(){
	// Initialize main controller on window load.
    window.addEventListener("load", () => {
	    let pageOrchestrator = new PageOrchestrator();
	    pageOrchestrator.refresh();
    });
    
    /**
	 * Main page controller
	 */
    class PageOrchestrator {   
        constructor() {
            this.userData = new UserData();
            this.taxonomy = new Taxonomy();
        };

        refresh() {
			this.userData.show();
			this.taxonomy.show();
		};
    }
	
	/**
	 * Data for the currently logged in user
	 * Controls the main title and the header contents
	 */
	class UserData {
        constructor() {
            this.id = localStorage.getItem("id");
			this.name = localStorage.getItem("name");
            this.idElements = [document.getElementById("header_id")], 
            this.nameElements = [document.getElementById("title_name"), document.getElementById("header_name")];
			
			// Setup logout listener
			let logout_button = document.getElementById("logout_button");
            logout_button.addEventListener("click", _ => localStorage.clear() );
        }
        
        show() {
			// Show User and Id text in respective ui elements
            this.idElements.forEach(element => element.textContent = this.id );
            this.nameElements.forEach(element => element.textContent = this.name );
        };
    }
    
    /**
	 * Data for taxonomy representation and modification.
	 */
    class Taxonomy {
		constructor() {
			this.taxonomy_container = document.getElementById("taxonomy_container");
			this.warning_div = document.getElementById("taxonomy_warning_msg");
			this.dialog_box = document.getElementById("dialog_box");
			this.dialog_title = document.getElementById("dialog_title");
			
			// Initialize Create Category form
			let create_button = document.getElementById("create_button");
    		let create_warning_div = document.getElementById("create_warning_msg");
    		attachForm(create_button, create_warning_div, "CreateCategory", request => {
				if (request.readyState == XMLHttpRequest.DONE) {
					switch(request.status){
		                case 200:
		                    this.show(); // Since this is an arrow function, 'this' refers to Taxonomy
		                    break;
		                case 400: // bad request  (fallthrough)
		                case 401: // unauthorized       |
		                case 500: // server error       v
		                    create_warning_div.textContent = request.responseText;
		                    create_warning_div.style.display = "block"; // Show error div
		                    break;
		                default: //Error
		                    create_warning_div.textContent = "Request reported status " + request.status;
		                    create_warning_div.style.display = "block"; // Show error div
		    		}
				}
			});
			
			// Initialize listeners for the various buttons:
			
			// Save button sends copy data to the DB
			let save_button = document.getElementById("save_button");
			this.save_div = document.getElementById("save_div");
			save_button.addEventListener("click", _ => {
				// Assemble request contents
				let copyForm = new FormData();
				copyForm.append("copySrcID", this.copySrcID);
				copyForm.append("copyTgtID", this.copyTgtID);
				
				// Notify the server of the copy
				makeCall("POST", "CopyCategory", copyForm, request => {
					if (request.readyState == XMLHttpRequest.DONE) {
						switch(request.status){
			                case 200: //Okay, rebuild the tree (do this because we then allow renaming/copying)
			                	this.show();
			                    break;
			                case 400: // bad request  (fallthrough)
			                case 401: // unauthorized       |
			                case 500: // server error       v
			                    this.update(); //Reset view
			                    this.warning_div.textContent = request.responseText;
			                    this.warning_div.style.display = "block"; // Show error div
			                    break;
			                default: //Error
			                    this.update(); //Reset view
			                    this.warning_div.textContent = "Request reported status " + request.status;
			                    this.warning_div.style.display = "block"; // Show error div
			    		}
					}
				})
				
				// Remove save button
				this.save_div.style.display = "none";
			})
			
			// Confirm button dispatches copy and pulls up save button
			let confirm_button = document.getElementById("confirm_button");
			confirm_button.addEventListener("click", _ => {
				// Remove old copy
				this.tempNodes.forEach(e => e.remove());
				this.tempNodes = new Array();
				
				// Execute new copy
				this.executeCopy();
				
				// Update ui
				dialog_box.style.display = "none";
				save_div.style.display = "block";
			})
			
			// Cancel button hides dialog box and resets to old src/tgt values if present
			let cancel_button = document.getElementById("cancel_button");
			cancel_button.addEventListener("click", _ => {
				dialog_box.style.display = "none";
				this.copySrcID = this.tempSrcID;
				this.copyTgtID = this.tempTgtID;
			});
		}
		
		/*
		 * Call the GetTaxonomy servlet to fetch the taxonomy and refresh the whole taxonomy tree.
		 * This must be called on every refresh because the taxonomy is shared
		 */
		show() {
			makeCall("GET", "GetTaxonomy", null, request => {
				if (request.readyState == XMLHttpRequest.DONE) {
					switch(request.status){
		                case 200: //Okay, save the taxonomy
		                    this.tree = JSON.parse(request.responseText);
		                    this.warning_div.style.display = "none"; // Reset error div
		                    this.update();
		                    break;
		                case 400: // bad request  (fallthrough)
		                case 401: // unauthorized       |
		                case 500: // server error       v
		                    this.warning_div.textContent = request.responseText;
		                    this.warning_div.style.display = "block"; // Show error div
		                    break;
		                default: //Error
		                    this.warning_div.textContent = "Request reported status " + request.status;
		                    this.warning_div.style.display = "block"; // Show error div
		    		}
				}
			})
		}
		
		/**
		 * Update the DOM to display the currently saved tree.
		 */
		update() {
			// Reset Copy functionality
			this.tempSrcID = null;
			this.tempTgtID = null;
			this.copySrcID = null;
			this.copyTgtID = null;
			this.tempNodes = new Array();
			this.save_div.style.display = "none";
			
			// Remove old taxonomy and form entries
			document.querySelectorAll(".node, option").forEach(e => e.remove());
			
			let create_dropdown = document.getElementById("create_dropdown");
			this.tree.forEach(category => {
				// Add category to create category form dropdown (if it doesn't have too many children)
				let option = null;
				if (category.childCount < 9) {
					option = document.createElement("option");
					option.value = category.id.toString();
					option.textContent = category.name;	
					create_dropdown.appendChild(option);
				}
				
				// Create node container
				let node = document.createElement("div");
				node.className = "node";
				node.style.marginLeft = (category.generation * 4).toString().concat("em");
				category.node = node; // add a node property to the category
				
				// Create node inner content div
				let nodeContent = document.createElement("div");
				nodeContent.className = "node-content";
				if (category.generation % 2 == 0) {
					nodeContent.className += " even";
				}

				// Split text content to insert renaming box later
				let nodeID = document.createElement("span");
				let nodeName = document.createElement("span");
				nodeID.textContent = category.id + " - ";
				nodeName.textContent = category.name;
				
				nodeContent.appendChild(nodeID);
				nodeContent.appendChild(nodeName);
				
				// Avoid making root node interactive
				if (category.id != 0) {
					// RENAME FUNCTIONALITY
					// Setup on click listener for renaming categories
					nodeName.addEventListener("click", _ => {
						// Hide old name
						nodeName.style.display = "none";
						
						// Create input box with the old name
						let nodeInput = document.createElement("input");
						nodeInput.value = nodeName.textContent;
						nodeInput.className = "node-input";
						nodeInput.maxLength = "45";
						
						nodeContent.appendChild(nodeInput);
						nodeInput.focus();
						
						// Remove input on focus loss
						nodeInput.addEventListener("focusout", _ => {
							let newName = nodeInput.value;
							
							nodeName.style.display = "inline";
							nodeContent.removeChild(nodeInput);
							
							// Assemble request contents
							let renameForm = new FormData();
							renameForm.append("id", category.id); // this won't change during execution
							renameForm.append("newName", newName);
							
							// Notify the server of the name change
							makeCall("POST", "RenameCategory", renameForm, request => {
								if (request.readyState == XMLHttpRequest.DONE) {
									switch(request.status){
						                case 200: //Okay, continue (also rename in create category form)
						            		nodeName.textContent = newName;
						                	if (option != null) {
												option.textContent = newName;
											}
											this.warning_div.style.display = "none"; // Reset error div
						                    break;
						                case 400: // bad request  (fallthrough)
						                case 401: // unauthorized       |
						                case 500: // server error       v
						                    this.warning_div.textContent = request.responseText;
						                    this.warning_div.style.display = "block"; // Show error div
						                    break;
						                default: //Error
						                    this.warning_div.textContent = "Request reported status " + request.status;
						                    this.warning_div.style.display = "block"; // Show error div
						    		}
								}
							})
						})
					});
					
					// DRAG FUNCTIONALITY
					nodeContent.draggable = "true";
					nodeContent.addEventListener("dragstart", e => {
						e.dataTransfer.setData("copySrcID", category.id.toString()); // keep the category's id in the event
						e.dataTransfer.dropEffect = "copy";
					})
				}
				
				// DROP FUNCTIONALITY
				nodeContent.addEventListener("dragover", e => e.preventDefault()); // override this or drop won't work
				
				// On drop, save copy/target ids and display dialog box.
				nodeContent.addEventListener("drop", e => {
					let src = e.dataTransfer.getData("copySrcID");
					let tgt = category.id.toString();
					
					// Don't copy to own subtree
					if (tgt.startsWith(src)) {
						this.warning_div.textContent = "A category cannot be copied to its own subtree!";
					    this.warning_div.style.display = "block";
						return;
					}
					
					// Don't copy when the target has too many children
					if (category.childCount >= 9) {
						this.warning_div.textContent = "Copying to a category with too many children!";
					    this.warning_div.style.display = "block";
						return;
					}
					
					this.warning_div.style.display = "none"; // reset warning div
					this.tempSrcID = this.copySrcID;
					this.tempTgtID = this.copyTgtID;
					this.copySrcID = src;
					this.copyTgtID = tgt;
					
					this.dialog_title.textContent = "Copying categories: " + this.copySrcID + " -> " + this.copyTgtID;
					this.dialog_box.style.display = "block";
				})
				
				node.appendChild(nodeContent);
				this.taxonomy_container.appendChild(node);
			})
		}
		
		/**
		 * Execute a local copy of the selected subtree
		 * The copied subtree will not be modifiable until it is saved to the server.
		 */
		executeCopy(){
			// Deep copy the subtree we are trying to copy
			let subtree = JSON.parse(JSON.stringify(
				this.tree.filter(category => category.id.toString().startsWith(this.copySrcID))
			));
			let rootID = parseInt(this.copyTgtID);
			let root = this.tree.find(category => category.id == rootID);
			
			// Relabel id and parentID for the subtree obtained
			for (let i = 0; i < subtree.length; ++i) {
				let parent = subtree[i];
				let newID;
				
				// Get the new id for the current category
				if ( i == 0 ) {
					parent.parentID = rootID;
					parent.generation = root.generation + 1;
					newID = rootID * 10 + root.childCount + 1;
				} else {
					newID = parent.parentID * 10 + parent.id % 10;
				}
				
				// Relabel all children of the current category
				for ( let j = i; j < subtree.length; ++j ) {
					let child = subtree[j];
					
					if ( child.parentID == parent.id ) {
						child.parentID = newID;
						child.generation = parent.generation + 1;
					}
				}
				parent.id = newID;
			}
			
			// Find the node before which we should insert the subtree
			let parentIndex = this.tree.indexOf(root);
			let nextCategory = null;
			
			if (parentIndex != 0) { //nextCategory remains null for root copies
				for (let i = parentIndex + 1; i < this.tree.length; ++i) {
					nextCategory = this.tree[i];
	
					if (!nextCategory.id.toString().startsWith(this.copyTgtID)) {
						break;
					}
				}								
			}
			
			// Add the nodes for the new subtree (we don't bother updating the actual taxonomy tree)
			// These nodes lack all listeners needed for interaction.
			// They are saved to an instance variable to be able to quickly remove them.
			let nextNode = nextCategory != null ? nextCategory.node : null;
			subtree.forEach(category => {
				// Create node container
				let node = document.createElement("div");
				node.className = "node";
				node.style.marginLeft = (category.generation * 4).toString().concat("em");

				// Create node inner content div (highlight them)
				let nodeContent = document.createElement("div");
				nodeContent.className = "node-content highlighted";
				if (category.generation % 2 == 0) {
					nodeContent.className += " even";
				}

				let nodeID = document.createElement("span");
				let nodeName = document.createElement("span");
				nodeID.textContent = category.id + " - ";
				nodeName.textContent = category.name;
				
				// Link up nodes
				nodeContent.appendChild(nodeID);
				nodeContent.appendChild(nodeName);
				node.appendChild(nodeContent);
				
				this.taxonomy_container.insertBefore(node, nextNode);
				this.tempNodes.push(node);
			})
		}
	}
    
})();
