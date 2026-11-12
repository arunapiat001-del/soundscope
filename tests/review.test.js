import test from 'node:test';import assert from 'node:assert/strict';import {CATEGORIES,scoreReview} from '../web/js/review.js';
test('weights add up to 100',()=>assert.equal(CATEGORIES.reduce((a,c)=>a+c[2],0),100));
test('empty is N/A, zero is a real score',()=>{assert.equal(scoreReview({}).score,null);assert.equal(scoreReview({vibe:'0'}).score,0)});
test('N/A categories are excluded from denominator',()=>{let r=scoreReview({vibe:8,vocal:4});assert.equal(r.score,6.4);assert.equal(r.coverage,25);assert.equal(r.rated.length,2);assert.equal(r.missing.length,8)});
test('rejects invalid scores',()=>{assert.throws(()=>scoreReview({vibe:11}));assert.throws(()=>scoreReview({vibe:-1}));assert.throws(()=>scoreReview({vibe:'nonsense'}))});
test('label follows rounded score without gaps',()=>{assert.equal(scoreReview({vibe:8.49}).label,'Luar biasa');assert.equal(scoreReview({vibe:9.85}).label,'Masterpiece');assert.equal(scoreReview({vibe:2.95}).label,'Di bawah rata-rata')});
