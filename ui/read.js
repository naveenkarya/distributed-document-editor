const modal = $('#exampleModal');

$('textarea#docContent').tinymce({
    height: 500,
    menubar: false,
    toolbar: 'undo redo | blocks | bold italic backcolor | ' +
      'alignleft aligncenter alignright alignjustify | ' +
      'bullist numlist outdent indent | removeformat | help',
    selector: 'textarea',
    init_instance_callback: function(editor){
        editor.mode.set("readonly"); // read only so can't edit content
        if(sessionStorage.getItem('currentDocumentID')){
            $.get("http://localhost:8080/document/"+sessionStorage.getItem('currentDocumentID'), function(data) { 
                $('input#docTitle').val(data.title); // set document title
                editor.setContent(data.content);  // set document content
            });
        }
    }
  });

$(function(){
    // forces user to click close button on modal
    modal.modal({backdrop: 'static', keyboard: false});
});

$('#editModeSwitch').on('change',function(){
    if(this.checked==true){
        modal.modal('show');
    }
});

$('button.btn-close').on('click', function(){
    modal.modal('hide');
    $('#editModeSwitch').prop('checked',false);
})

$('button#edit-request').on('click', function(){
    const user = $('input#user-id').val();
    if(user){
        console.log(`Checking with leader for user ${user}...`);
        // TODO: check with leader
        window.location = './write.html'; // redirect to write page if successful
        // give error message if leader doesn't allow write
    } else {
        console.log('No email provided')
    }
})