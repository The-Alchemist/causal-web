function fileTypeAction(chkOpt) {
    var id = $(chkOpt).attr('value');
    if (id == dataTypeId) {
        $('#datasetOptions').removeClass('none');
    } else {
        $('#datasetOptions').addClass('none');
    }
}
$(document).ready(function () {
    $("#view_file_info").show();
    $("#edit_file_info").hide();

    $("#edit_info").click(function () {
        $("#view_file_info").hide();
        $("#edit_file_info").show();
    });
    $("#cancel_info").click(function () {
        $("#view_file_info").show();
        $("#edit_file_info").hide();
    });

    fileTypeAction($("input[name='fileTypeId']:checked"));
});
$(document).on('click', '#categorize_file', function (e) {
    collapseAction(this);
});