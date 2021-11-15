/**
 * Generates index.html page for docs application
 * 
 * @param {string} version 
 * @param { {href: string, label: string}[] } links 
 */
function index(title, version, links) {
  return `
    <!DOCTYPE html>
    <html lang="en">
    <title>${title} version@${version}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
    <link href='https://fonts.googleapis.com/css?family=RobotoDraft' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"><style>
    html,body,h1,h2,h3,h4,h5 {font-family: "RobotoDraft", "Roboto", sans-serif}
    .w3-bar-block .w3-bar-item {padding: 16px}
    </style>
    <body>
    
    <!-- Side Navigation -->
    <nav class="w3-sidebar w3-bar-block w3-collapse w3-white w3-animate-left w3-card" style="z-index:3;width:320px;" id="mySidebar">
    <a href="javascript:void(0)" class="w3-bar-item w3-button w3-border-bottom w3-large">${title} <sup>version@${version}</sup></a>
    <a href="javascript:void(0)" onclick="w3_close()" title="Close Sidemenu" 
  class="w3-bar-item w3-button w3-hide-large w3-large">Close <i class="fa fa-remove"></i></a>
    ${links.map(l => link(l.href, l.label)).join('\n')}
    </nav>
    
    <!-- Page content -->
    <div class="w3-main" style="margin-left:320px;">
    <i class="fa fa-bars w3-button w3-white w3-hide-large w3-xlarge w3-margin-left w3-margin-top" onclick="w3_open()"></i>
    
    <div class="w3-container person" style="padding: 0px; height: 99vh;">
    <iframe 
        name="docs"
        style="padding: 0px; width: 100%; height: 99vh;" 
        src="${links[0].href}">
    </iframe>
    </div>
         
    </div>
    <script>
    function w3_open() {
        document.getElementById("mySidebar").style.display = "block";
        document.getElementById("myOverlay").style.display = "block";
      }
      
      function w3_close() {
        document.getElementById("mySidebar").style.display = "none";
        document.getElementById("myOverlay").style.display = "none";
      }
    </script>
`
}

function link(href, label) {
  return `<a target="docs" class="w3-bar-item w3-button" href="${href}">${label}</a>`
}

module.exports = index 