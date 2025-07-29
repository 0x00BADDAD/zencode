import _ from 'lodash';
import './style.css';
import Icon from './icon.png';
import printMe from './print.js';


function component() {
  const element = document.createElement('div');

 // Lodash, now imported by this script
  element.innerHTML = _.join(['Hello', 'webpack'], ' ');
  const btn = document.createElement('button');


  element.classList.add('hello');

  // Add the image to our existing div
  const myIcon = new Image();
  myIcon.src = Icon;

  element.appendChild(myIcon);


  btn.innerHTML = 'Click me and check the console!';
  btn.onclick = printMe;

  element.appendChild(btn);

  return element;
}

document.body.appendChild(component());
