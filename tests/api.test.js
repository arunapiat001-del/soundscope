import test from 'node:test';import assert from 'node:assert/strict';import {handler} from '../netlify/functions/api.js';
process.env.APP_ORIGINS='https://example.netlify.app';process.env.MUSICBRAINZ_CONTACT='demo@example.com';delete process.env.AUDD_API_TOKEN;
const req=(op,more={})=>({httpMethod:'GET',headers:{origin:'https://example.netlify.app'},queryStringParameters:{op,...more}});
test('CORS blocks unknown origin',async()=>{let e=req('status');e.headers.origin='https://evil.example';let r=await handler(e);assert.equal(r.statusCode,403);assert.equal(r.headers['Access-Control-Allow-Origin'],undefined)});
test('status exposes no secret and Android origin allowed',async()=>{let e=req('status');e.headers.origin='https://appassets.androidplatform.net';let r=await handler(e);let b=JSON.parse(r.body);assert.equal(r.statusCode,200);assert.equal(b.metadata,true);assert.equal(b.recognition,false);assert.equal(r.body.includes('demo@example.com'),false)});
test('arbitrary URLs are not accepted in detail',async()=>{let r=await handler(req('detail',{type:'recording',id:'https://example.org/'}));assert.equal(r.statusCode,400)});
test('recognition is disabled without secret',async()=>{let r=await handler({...req('recognize'),httpMethod:'POST',body:'{}'});assert.equal(r.statusCode,503)});
test('same-origin request can omit origin',async()=>{let e=req('status');e.headers={'sec-fetch-site':'same-origin'};assert.equal((await handler(e)).statusCode,200)});
