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
      'bullist numlist outdent indent | removeformat | help'
  });

$('button#save').on('click', function(){
    // TODO: pull document ID from database with previously saved content
    saveDoc(0, $('input#docName').val(), tinymce.activeEditor.getContent());
});

$('#editModeSwitch').on('change',function(){
    if(this.checked==false){
        // TODO: add modal, confirm save/ignore changes, redirect to read
    }
});

function saveDoc(id, name, content){
    console.log(`Sending content for document ${id} to db`);
    console.log(`Document name: ${name}`);
    console.log(`Document content:\n${content}`);
    // TODO: write to database
}