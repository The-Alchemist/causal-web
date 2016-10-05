$(document).ready(function () {
    $('#submitFeedback').prop('disabled', $('#feedbackMsg').val().length === 0);
});
$('#feedbackMsg').on("input", function () {
    if ($('#feedbackMsg').val().length > 0) {
        $('#submitFeedback').removeAttr('disabled');
    } else {
        $('#submitFeedback').prop('disabled', true);
    }
});
