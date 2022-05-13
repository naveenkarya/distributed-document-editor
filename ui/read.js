const modal = $('#exampleModal');

$(function(){
    // forces user to click close button
    modal.modal({backdrop: 'static', keyboard: false});
});

$('#editModeSwitch').on('change',function(){
    if(this.checked==true){
        modal.modal('show');
    }
});

$('button.close').on('click', function(){
    modal.modal('hide');
    $('#editModeSwitch').prop('checked',false);
})

$('button#edit-request').on('click', function(){
    const user = $('input#user-id').val();
    if(user){
        console.log(`Checking with leader for user ${user}...`);
        // TODO: check with leader, redirect to write page if successful
    } else {
        console.log('No email provided')
    }
})