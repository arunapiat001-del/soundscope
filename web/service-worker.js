const CACHE='soundscope-shell-v2';
const SHELL=['./','./index.html','./config.js','./css/style.css','./js/app.js','./js/review.js','./manifest.webmanifest','./assets/icon-192.png','./assets/icon-512.png'];
self.addEventListener('install',e=>e.waitUntil(caches.open(CACHE).then(c=>c.addAll(SHELL)).then(()=>self.skipWaiting())));
self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k.startsWith('soundscope-shell-')&&k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',e=>{const u=new URL(e.request.url);if(e.request.method!=='GET'||u.origin!==self.location.origin||!SHELL.some(x=>new URL(x,self.registration.scope).pathname===u.pathname))return;e.respondWith(caches.match(e.request).then(cached=>cached||fetch(e.request)));});
