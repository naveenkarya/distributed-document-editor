const template = $('tr#template');
// template.hide();

$.get("http://localhost:8080/document/all", function(data) {
    for(document of data){
        // assuming document={name: , }
        const newDoc = template.clone();
        newDoc.find('td.doc-name').html(document.title);
        newDoc.find('td.version').html(document.version);
        newDoc[0].id=document.id;
        $("tbody").append(newDoc);
        newDoc.show();
    }
});

// $()

// $('button#new-doc').on('click', function(){
//     console.log("creating new doc");
//     $.ajax({
//         method: 'POST',
//         url: "http://localhost:8080/document/add",
//         complete: function(){ // should be success
//             window.location = './write.html';
//         },
//     });
// });

$('button#new-doc').on('click', function(){
    // pop up here
    window.location = './write.html';
});