const http = require('http');
const fs = require('fs')

let allTheData = {};
let log = "";

function degToRad(deg){
  let pi = Math.PI;
  return deg * (pi/180);
}

function tryToWriteFile(fileName, contenu){
  fs.writeFile(fileName, contenu, err => {
    if (err) {
      console.log('Error writing file '+fileName, err)
    } else {
      console.log(fileName+' saved')
    }
  })
}

function save(){
  let now = Date.now();

  if(now >= nextSave){
    let jsonString = JSON.stringify(allTheData);
    console.log("Sauvegarde des données");

    tryToWriteFile('./data.json', jsonString);
    tryToWriteFile('./log.txt', log);

    nextSave = now + 1000 * minDelayBetweenSaves;
  }
}


try {
  allTheData = require("./data.json");
} catch (error) {
  console.log("Aucun fichier de données détecté");
}

fs.readFile("./log.txt", 'utf8' , (err, data) => {
  if (err) {
    console.error("Aucun fichier de logs détecté" + err)
    return
  }
  log = data;
})




//let allTheData =  {};

const minDelayBetweenSaves = 30 ; //seconds
let nextSave = Date.now() + 1000 * minDelayBetweenSaves;

//const jsonString = JSON.stringify(customer);


const requestListener = function (req, res) {
  
    if (req.method == 'POST') {
      let rawData = req.url;
      let type = rawData.split("/")[1];

      if(type == "sendimage"){
        try{
          console.log('POST')
          var body = ''
          req.on('data', function(data) {
            body += data
          })
          req.on('end', function() {
            let data = JSON.parse(body);
            let uuid = data.uuid;
            tryToWriteFile("images/"+uuid,data.data)

            res.writeHead(200, {'Content-Type': 'text/html'})
            res.end('post received')
          })
        }catch{

        }
      }
      else if(type == "sendsms"){ 
        //sms
        try{
          console.log('POST_SMS')
          var body = ''
          req.on('data', function(data) {
            body += data;
          })
          req.on('end', function() {
            let data = JSON.parse(body);
            let uuid = data.uuid;

            allTheData["children"][uuid]["sms"].push(data.data);
        
            res.writeHead(200, {'Content-Type': 'text/html'})
            res.end('post received')
          })
        }catch{}
        
        
      }
      else if(type == "sendcontact"){
        try{
          var body = ''
          req.on('data', function(data) {
            body += data;
          })
          req.on('end', function() {
            let data = JSON.parse(body);
            let uuid = data.uuid;

            allTheData["children"][uuid]["contacts"].push(data.data);
        
            res.writeHead(200, {'Content-Type': 'text/html'})
            res.end('post received')
          })
        }catch{}

      }
      else{
        console.log("unknoiwn type : "+type);
      } 
    }
    else{
    let rawData = req.url;

    console.log("raw : "+rawData);
    
    let type = rawData.split("/")[1];

    if(type == "SET"){
      let alert = false;
      try{
        //ajout de la position de l'enfant
        let now = Date.now();
        let data = rawData.split("/")[2].split(";").map(a=>a.split("=")).reduce((a,b)=>{a[b[0]]=b[1];return a;},{});

        if(allTheData["data"][data.uuid] == undefined){
          allTheData["data"][data.uuid] = []
        }
        allTheData["data"][data.uuid].push({"time":now,"lat":data.lat,"lng":data.lng})

        //Test si un enfant est dans au moin une zone

        let allTheZones = [];
        
        for(const [key, p] of Object.entries(allTheData["parents"])){
          if(p.children.includes(data.uuid)){
            for(z of p["zone"]){
              allTheZones.push(z);
            }
          }
        }

        let lat = degToRad(data.lat);
        let lon = degToRad(data.lng);

        let child_x = 6371  * Math.cos(lat) * Math.cos(lon);
        let child_y = 6371  * Math.cos(lat) * Math.sin(lon);
        let child_z = 6371  * Math.sin(lat);

        for(z of allTheZones){
          let lat = degToRad(z.lat);
          let lon = degToRad(z.lon);

          let zone_x = 6371 * Math.cos(lat) * Math.cos(lon); 
          let zone_y = 6371 * Math.cos(lat) * Math.sin(lon); 
          let zone_z = 6371 * Math.sin(lat);
          
          let dist = Math.sqrt(Math.pow(child_x-zone_x,2)+Math.pow(child_y-zone_y,2)+Math.pow(child_z-zone_z,2));

          if(dist<z.rad){
            alert = true;
          }
        }
      }catch{}

      res.writeHead(200);
      res.end(alert?'alert':'ok');
    }
    
    else if(type == "REGISTERCHLID"){//un enfant qui créé sun compte
      //"/REGISTERCHLID/uuid;kevin;dupont;12"

      let data = rawData.split("/")[2].split(";")
      allTheData["children"][data[0]] = {"name":data[1],"surname":data[2],"age":data[3],"contacts":[],"sms":[]}

      res.writeHead(200);
      res.end('done')
    }
    else if(type == "REGISTERPARENT"){//Création du compte d'un parent

      let code = Math.round(100000+Math.random()*(999999-100000)).toString(10);
      console.log("code : "+code);

      try{
        let data = rawData.split("/")[2].split(";")
        allTheData["parents"][data[0]] = {"name":data[1],"surname":data[2],"mail":data[3],"mdp":data[4],"children":[],"code":code,"zone":[]}
      }catch{

      }
      
      res.writeHead(200);
      res.end(code)
    }
    else if(type == "SETASPARENT"){
      //Ajout d'un parent à un enfant
      console.log(rawData);
      let error = true;

      let prenomParent = '';
      let nomParent = '';
      
      try{
        let splitedData = rawData.split("/")[2].split(";");

        let code = splitedData[1];
        let uuid = splitedData[0];

        console.log("uuid : " + uuid);
        console.log("code : " + code);

        for([key, value] of Object.entries(allTheData["parents"])){
          if(value["code"] == code){
            error = false;
            let add = true;
            for(s of allTheData["parents"][key]["children"]){
              if (s == uuid){
                add = false;
              }
            }
            if(add){
              allTheData["parents"][key]["children"].push(uuid);
              //Ajout
            }else{
              //"Déja ajouté"
            }
            nomParent = allTheData["parents"][key].surname;
            prenomParent = allTheData["parents"][key].name;
          }
        }
      }catch{}

      console.log("parents : "+nomParent+" "+prenomParent);

      res.writeHead(200);
      res.end(error ? "error" : prenomParent+" "+nomParent)
    }
    else if(type == "GETMYCHILDREN"){
      //Envoi des position des enfants d'un parent
      let dataTosend = [];

      try{
        let uuid = rawData.split("/")[2].split(";")[0];
        let children = allTheData["parents"][uuid]["children"];    

        console.log(children);

        for(o of children){
          console.log("o c'est ça : "+o);
          dataTosend.push({
            "uuid":o,
            "name":allTheData["children"][o].name,
            "surname":allTheData["children"][o].surname,
            "age":allTheData["children"][o].age,
            "data":allTheData["data"][o]
          })
        }
      }catch{}
      
      res.writeHead(200);
      res.end(JSON.stringify(dataTosend));
    }
    else if(type == "HAVEANACCOUNT"){//au lancement de l'application on demande au serveur si le mobile est déjà associé à un compte
      //http://www.achline.fr/HAVEANACCOUNT/uuid
      //console.log("Test si un utilisateur a un compte")

      let dataTosend = "";

      try{
        let uuid = rawData.split("/")[2].split(";")[0];
        
        if(uuid in allTheData["parents"]){
          dataTosend = "parent";
          console.log("Connexion d'un utilisateur : "+allTheData["parents"][uuid]);
        }else if(uuid in allTheData["children"]){
          dataTosend = "child";  
          console.log("Connexion d'un utilisateur : "+allTheData["children"][uuid]);
        }else{
          dataTosend = "no";
          console.log("Connexion d'un nouvel utilisateur : "+uuid);
        } 
      }catch{}
      console.log("envoi : "+dataTosend);
      res.writeHead(200);
      res.end(dataTosend);
    }
    else if(type == "ADDZONE"){
      //Ajout d'une zone
      try{
        let data = rawData.split("/")[2].split(";");

        allTheData["parents"][data[0]]["zone"].push({
          "lat":parseFloat(data[2]),
          "lon":parseFloat(data[1]),
          "rad":parseFloat(data[3])
        });

      }catch{}

      res.writeHead(200);
      res.end('done')
    }
    else if(type == "GETIMAGE"){
      try{
        let data = rawData.split("/")[2].split(";");
        let uuid = data[0];
        let ind = parseInt(data[1]);

        let currentChild = "";
        
        for(child of allTheData["parents"][uuid]["children"]){
          console.log("currentChild : "+currentChild);          
          if(fs.existsSync("images/"+child)){
            if(ind == 0){
              currentChild = child;
              break;
            }else{
              ind--;
            }
          }
        }
        if(currentChild != ""){
          fs.readFile("images/"+currentChild, 'utf8' , (err, data) => {
            if (err) {
              console.error("Aucun fichier de logs détecté" + err)
              res.writeHead(200);
              res.end("end")
              return
            }
            res.writeHead(200);
            res.end(data)
          })
        }else{
          res.writeHead(200);
          res.end("end")
        }
      }catch{}
      
    }
    else if(type == "GETSMS"){
      let tosend = [];
      try{
        let uuid = rawData.split("/")[2].split(";")[0];

        for(child of allTheData["parents"][uuid]["children"]){
          for(sms of allTheData["children"][child]["sms"]){
            tosend.push(sms);
          }
        }

        res.writeHead(200);
        res.end(JSON.stringify(tosend));

      }catch{
        res.writeHead(200);
        res.end("erreur");
      }
    }
    else if(type == "GETCONTACT"){
      let tosend = [];
      try{
        let uuid = rawData.split("/")[2].split(";")[0];

        for(child of allTheData["parents"][uuid]["children"]){
          for(contact of allTheData["children"][child]["contacts"]){
            tosend.push(contact);
          }
        }
        
        res.writeHead(200);
        res.end(JSON.stringify(tosend));

      }catch{
        res.writeHead(200);
        res.end("erreur");
      }
    }
    else{
      if(rawData != "/favicon.ico" && rawData != "/"){
        console.log("\x1b[5m\x1b[31m", "Suspicious : \x1b[0m\x1b[41m\x1b[37m"+rawData,"\x1b[0m");  //cyan
        log += rawData + "\n";

        const trollUrl = ["https://www.youtube.com/watch?v=1gu3W6-GHiw","https://www.youtube.com/watch?v=dQw4w9WgXcQ", "https://www.youtube.com/watch?v=QLWeDcTKYYA", "https://www.youtube.com/watch?v=QVYSsn_HL1w","https://youtu.be/6-xEyHYOXAs?t=19","https://www.youtube.com/watch?v=1nIEk047YXw","https://www.youtube.com/watch?v=aIVsz5Pj0eE","https://www.youtube.com/watch?v=rJ1wooLVyvE","https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000030939438/","https://youtu.be/Uz2X9vtGkgI?t=295","https://www.youtube.com/watch?v=TgFYdzRx7wg","https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwjeyaKG1vr3AhXV8LsIHYdNB9IQFnoECAUQAQ&url=https%3A%2F%2Fhal.archives-ouvertes.fr%2Fhal-03628382%2Fdocument&usg=AOvVaw1HOQEmFexh-QD-5lJsSBJq","https://youtu.be/st8xWyMZXOU?t=12"];
        const random = trollUrl[Math.floor(Math.random() * trollUrl.length)];

        res.writeHead(404);
        res.end('<!DOCTYPE html><html><head><title>HTML Meta Tag</title><meta http-equiv = "refresh" content = "1; url = '+random+'" /></head><body><p>Don\'t hack me please :\')</p></body></html>');
      }
    }
  }
  save();
  return 0 ;
}

const server = http.createServer(requestListener);
server.listen(80);

