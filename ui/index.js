// $.get("http://localhost:8080/document/add", function(data) {
    
// });

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
    window.location = './write.html';
});