$('textarea#tiny').tinymce({
    height: 500,
    menubar: false,
    selector: 'textarea',
    plugins: [
      'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
      'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
      'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount'
    ],
    toolbar: 'undo redo | blocks | bold italic backcolor | ' +
      'alignleft aligncenter alignright alignjustify | ' +
      'bullist numlist outdent indent | removeformat | help',
    init_instance_callback: function(editor){
        // TODO: get content from database
        editor.setContent("<p>Hello world</p>");  
    }
  });

const modal = $('#exampleModal');

$(function(){
    // forces user to click close button
    modal.modal({backdrop: 'static', keyboard: false});
});

$('#editModeSwitch').on('change',function(){
    if(this.checked==false) modal.modal('show');
});

$('button.close').on('click', function(){
    modal.modal('hide');
    $('#editModeSwitch').prop('checked',true);
})

$('button#save').on('click', function(){
    // TODO: pull document ID from database
    saveDoc(0, $('input#docName').val(), tinymce.activeEditor.getContent());
});

$('button#save-changes').on('click', function(){
    saveDoc(0, $('input#docName').val(), tinymce.activeEditor.getContent());
    returnToRead();
});

$('button#ignore-changes').on('click', function(){
    console.log("Danger! Not saving");
    returnToRead();
});

function saveDoc(id, name, content){
    console.log(`Sending content for document ${id} to db`);
    console.log(`Document name: ${name}`);
    console.log(`Document content:\n${content}`);
    // TODO: write to database
    // new document or update?
    if(!$.session.get("email")){
        // make pop up for email
        // 
    }
}

// TODO: return to read page
function returnToRead(){}