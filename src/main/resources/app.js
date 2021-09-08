/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var imgs = ['ronaldinho.jpg','minion.jpg','kimetsu.jpg'];
var posicion = 0;

function actualizar(){
    var image = document.getElementById('imagen');
    image.src = imgs[posicion];
}

function siguienteImagen(){
    posicion += 1;
    if(posicion>2){
        posicion=0;
    }
    actualizar();
}
function anteriorImagen(){
    posicion-=1;
    if(posicion<0){
        posicion=2;
    }
    actualizar();
}
