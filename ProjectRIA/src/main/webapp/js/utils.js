/**
 * Collection of utility functions used by other script files
 */


/**
 * Attach click listener to a form
 */
function attachForm(form_button, error_div, request_url, callback) {
	form_button.addEventListener("click", e => {
    	let form = e.target.closest("form"); 
        error_div.style.display = "none"; // Hide error div
        if (form.checkValidity()) { // Do form check
        	makeCall("POST", request_url, form, callback) // Send ajax call
        }else 
            form.reportValidity(); // If not valid, notify
    });
}

/**
 * Submit forms on enter input from user
 * Attach a listener for 'Enter' key input to all input fields of form, overriding
 * the default submit action
 */
(function (){
    let forms = document.getElementsByTagName("form");
    Array.from(forms).forEach(form => {
        let input_fields = form.querySelectorAll("input:not([type='button']):not([type='hidden'])");
        let button = form.querySelector("input[type='button']");
        Array.from(input_fields).forEach(input => {
            input.addEventListener("keydown", (e) => {
                if(e.key === "Enter"){
                    e.preventDefault(); // Default form action is submit, prevent this
                    let click = new Event("click"); // Dispatch click event for submit button
                    button.dispatchEvent(click);
                }
            });
        });
    });
})();
 
/**
 * AJAX call management
 * Works both for FormElements and FormData objects
 */
function makeCall(method, relativeUrl, form, callback, reset = true) {
    let req = new XMLHttpRequest(); // Create request
    req.onreadystatechange = function() {
      	callback(req) // pull request into closure
    };
    
    // Open the async request
    req.open(method, relativeUrl, true);
    if (form == null) {
    	req.send(); // send empty form
    } else if (form instanceof FormData) {
    	req.send(form); // Send serialized form
    } else {
		req.send(new FormData(form)) // Serialize form
	}
	
	// If the form is a ui element, eventually reset its fields
    if (form !== null && !(form instanceof FormData) && reset) {
      	form.reset();
    }
}
