'use strict';
(() => {
  const $ = id => document.getElementById(id);
  const state = { mode: 'login', token: '', user: null, runs: [], editId: null, deleteId: null, loading: false, loaded: false, busy: false };
  const tokenKey = 'runshot.access-token';
  try { state.token = sessionStorage.getItem(tokenKey) || ''; } catch (_) { /* 메모리만 사용 */ }
  const setToken = token => { state.token = token || ''; try { token ? sessionStorage.setItem(tokenKey, token) : sessionStorage.removeItem(tokenKey); } catch (_) {} };
  const dateValue = (d = new Date()) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  const duration = seconds => { const h = Math.floor(seconds/3600), m = Math.floor(seconds%3600/60), s = seconds%60; return `${h ? h+'시간 ' : ''}${m}분 ${s ? s+'초' : ''}`.trim(); };
  const pace = seconds => `${Math.floor(seconds/60)}′${String(seconds%60).padStart(2,'0')}″`;
  const distance = value => Number(value).toLocaleString('ko-KR',{maximumFractionDigits:2});
  const errorAt = (id,message) => { $(id).textContent=message || ''; $(id).hidden=!message; };
  let toastTimer;
  function toast(message) { clearTimeout(toastTimer); $('toast').textContent=message; $('toast').hidden=false; toastTimer=setTimeout(()=>{$('toast').hidden=true;},3500); }
  async function api(path,{method='GET',body,authenticated=true}={}) {
    const headers={Accept:'application/json'};
    if(body!==undefined) headers['Content-Type']='application/json';
    if(authenticated&&state.token) headers.Authorization=`Bearer ${state.token}`;
    let response;
    try { response=await fetch(path,{method,headers,body:body===undefined?undefined:JSON.stringify(body),cache:'no-store',signal:AbortSignal.timeout(15000)}); }
    catch(_) { throw new Error('서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.'); }
    const isJson=(response.headers.get('content-type')||'').includes('json');
    let data=null;
    try { data=response.status===204?null:isJson?await response.json():null; } catch(_) {}
    if(!response.ok) {
      if(response.status===401&&authenticated) { logout(); errorAt('auth-error','로그인이 만료되었습니다. 다시 로그인해 주세요.'); }
      const message=data?.message || (response.status===503?'서버 연결을 준비 중입니다. 관리자에게 문의해 주세요.':response.status===404?'요청한 데이터를 찾을 수 없습니다.':'요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.');
      const details=data?.fields?Object.keys(data.fields).map(k=>({email:'이메일',password:'비밀번호',nickname:'닉네임',distance:'거리',runningTime:'시간',runningDate:'날짜',content:'메모'}[k]||k)).join(', '):'';
      throw new Error(message+(details?` 확인할 항목: ${details}`:''));
    }
    if(response.status!==204&&!isJson) throw new Error('API 서버의 응답을 확인할 수 없습니다.');
    return data;
  }
  function changeMode(mode) {
    if(state.busy) return;
    state.mode=mode;
    const signup=mode==='signup';
    $('login-tab').setAttribute('aria-selected',String(!signup)); $('signup-tab').setAttribute('aria-selected',String(signup));
    $('nickname-field').hidden=!signup; $('nickname').required=signup; $('password').minLength=signup?8:1;
    $('password').setAttribute('autocomplete',signup?'new-password':'current-password');
    $('password-hint').hidden=!signup;
    $('auth-title').textContent=signup?'나만의 러닝을 시작해요.':'다시 만나서 반가워요.';
    $('auth-description').textContent=signup?'계정을 만들고 첫 번째 발자국을 남겨 보세요.':'로그인하고 오늘의 러닝을 이어가세요.';
    $('auth-submit').textContent=signup?'회원가입 ↗':'로그인 ↗'; errorAt('auth-error','');
  }
  function authBusy(busy) { state.busy=busy; ['auth-submit','login-tab','signup-tab'].forEach(id=>{$(id).disabled=busy;}); $('auth-submit').textContent=busy?'잠시만 기다려 주세요…':state.mode==='signup'?'회원가입 ↗':'로그인 ↗'; }
  $('login-tab').onclick=()=>changeMode('login'); $('signup-tab').onclick=()=>changeMode('signup');
  $('password-toggle').onclick=()=>{const show=$('password').type==='password';$('password').type=show?'text':'password';$('password-toggle').textContent=show?'숨기기':'보기';$('password-toggle').setAttribute('aria-pressed',String(show));$('password-toggle').setAttribute('aria-label',show?'비밀번호 숨기기':'비밀번호 표시');};
  $('auth-form').onsubmit=async event=>{
    event.preventDefault(); if(state.busy) return;
    const email=$('email').value.trim(), password=$('password').value;
    if(state.mode==='signup'&&new TextEncoder().encode(password).length>72) return errorAt('auth-error','비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.');
    if(state.mode==='signup'&&!$('nickname').value.trim()) return errorAt('auth-error','닉네임을 입력해 주세요.');
    errorAt('auth-error',''); authBusy(true);
    let signedUp=false;
    try {
      if(state.mode==='signup') {await api('/api/users',{method:'POST',body:{email,password,nickname:$('nickname').value.trim()},authenticated:false}); signedUp=true;}
      const login=await api('/api/auth/login',{method:'POST',body:{email,password},authenticated:false});
      setToken(login.accessToken); state.user={id:login.userId,email:login.email,nickname:login.nickname}; $('password').value='';
      showApp(); await loadRuns(); if(signedUp) toast('가입이 완료되었습니다. 첫 기록을 남겨 보세요.');
    } catch(error) {errorAt('auth-error',(signedUp?'회원가입은 완료되었습니다. 로그인해 주세요. ':'')+error.message);}
    finally {authBusy(false);}
  };
  function showApp() {
    $('auth-screen').hidden=true; $('app-screen').hidden=false;
    $('profile-name').textContent=state.user.nickname;
    $('avatar').textContent=Array.from(state.user.nickname)[0]||'R';
    $('welcome-text').textContent=`${state.user.nickname}님, 달린 만큼 나만의 이야기가 쌓여요.`;
    $('account-nickname').textContent=state.user.nickname; $('account-email').textContent=state.user.email;
  }
  function logout() {
    state.busy=false;
    setToken(''); state.user=null; state.runs=[]; state.loaded=false; state.editId=null; state.deleteId=null;
    document.querySelectorAll('dialog[open]').forEach(dialog=>dialog.close());
    $('app-screen').hidden=true; $('auth-screen').hidden=false; $('password').value='';
    $('records').replaceChildren(); changeMode('login');
  }
  $('logout').onclick=()=>{logout();toast('로그아웃했습니다.');};
  $('account-button').onclick=()=>$('account-dialog').showModal();
  $('journal-nav').onclick=()=>$('journal-title').scrollIntoView({behavior:'smooth'});
  async function loadRuns() {
    if(state.loading||!state.user) return;
    state.loading=true; $('loading').hidden=false; $('empty').hidden=true; $('records').hidden=true; $('refresh').disabled=true; errorAt('list-error','');
    try {
      const userId=state.user.id;
      const runs=await api('/api/runs');
      if(!state.user||state.user.id!==userId) return;
      state.runs=runs; state.loaded=true; render();
    } catch(error) {if(state.user) errorAt('list-error',error.message);}
    finally {state.loading=false;$('loading').hidden=true;$('refresh').disabled=false;$('records').hidden=false;}
  }
  $('refresh').onclick=loadRuns;
  const esc=value=>String(value??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  function render() {
    const month=$('month-filter').value, query=$('search').value.trim().toLowerCase();
    const runs=state.runs.filter(r=>(!month||r.runningDate.startsWith(month))&&(!query||(r.content||'').toLowerCase().includes(query)));
    const total=runs.reduce((sum,r)=>sum+r.distance,0), time=runs.reduce((sum,r)=>sum+r.runningTime,0);
    $('total-distance').textContent=distance(total);$('total-runs').textContent=runs.length;
    $('average-pace').textContent=total?pace(Math.round(time/total)):'—';$('record-count').textContent=runs.length;
    $('records').innerHTML=runs.map(r=>{
      const [year,month,day]=r.runningDate.split('-');
      return `<article class="record"><time class="date-tile" datetime="${esc(r.runningDate)}" aria-label="${year}년 ${month}월 ${day}일"><span class="month">${year}.${month}</span><span class="day">${Number(day)}</span></time><div><div class="run-data"><span class="distance">${distance(r.distance)}<small>km</small></span><span class="run-detail"><small>시간</small>${esc(duration(r.runningTime))}</span><span class="run-detail"><small>페이스</small>${pace(r.pace)} /km</span></div>${r.content?`<p class="run-note">${esc(r.content)}</p>`:''}</div><div class="run-actions"><button type="button" data-action="edit" data-id="${r.id}" aria-label="${r.runningDate} ${distance(r.distance)}km 기록 수정">수정</button><button type="button" data-action="delete" data-id="${r.id}" aria-label="${r.runningDate} ${distance(r.distance)}km 기록 삭제">삭제</button></div></article>`;
    }).join('');
    $('empty').hidden=runs.length>0||!state.loaded;
    const filtered=Boolean(month||query);
    $('empty-title').textContent=filtered?'조건에 맞는 기록이 없어요.':'첫 번째 러닝을 남겨 보세요.';
    $('empty-description').textContent=filtered?'기간이나 검색어를 바꾸거나, 새로운 러닝을 남겨 보세요.':'짧은 달리기도 소중한 기록이 됩니다.';
    $('empty-add').textContent=filtered?'새 기록 추가하기':'첫 기록 추가하기';
  }
  $('month-filter').onchange=render;$('search').oninput=render;$('all-months').onclick=()=>{$('month-filter').value='';render();};
  function runBusy(busy) { $('save-run').disabled=busy; $('save-run').textContent=busy?'저장 중…':'기록 저장'; $('run-form').querySelectorAll('input,textarea,button[data-close]').forEach(el=>{el.disabled=busy;}); }
  function totalSeconds() {return Number($('hours').value)*3600+Number($('minutes').value)*60+Number($('seconds').value);}
  function previewPace() {const seconds=totalSeconds(),km=Number($('distance').value);$('pace-preview').textContent=seconds>0&&km>0?pace(Math.round(seconds/km))+' /km':'— /km';$('content-length').textContent=$('content').value.length;}
  function openRun(run) {
    state.editId=run?.id??null; $('run-form').reset(); runBusy(false); errorAt('run-error','');
    $('run-dialog-title').textContent=run?'러닝 기록 수정':'러닝 기록 추가';
    $('running-date').max=dateValue();$('running-date').value=run?.runningDate??dateValue();$('distance').value=run?.distance??'';
    const seconds=run?.runningTime??1800;
    $('hours').value=Math.floor(seconds/3600);$('minutes').value=Math.floor(seconds%3600/60);$('seconds').value=seconds%60;
    $('content').value=run?.content??'';previewPace();$('run-dialog').showModal();
  }
  $('add-run').onclick=()=>openRun();$('empty-add').onclick=()=>openRun();$('run-form').oninput=previewPace;
  document.querySelectorAll('[data-close]').forEach(button=>button.onclick=()=>$(button.dataset.close).close());
  $('run-dialog').addEventListener('cancel',event=>{if($('save-run').disabled)event.preventDefault();});
  $('delete-dialog').addEventListener('cancel',event=>{if($('confirm-delete').disabled)event.preventDefault();});
  $('run-form').onsubmit=async event=>{
    event.preventDefault(); if($('save-run').disabled) return;
    const seconds=totalSeconds(); if(seconds<=0||seconds>604800) return errorAt('run-error','달린 시간은 1초 이상, 7일 이하여야 합니다.');
    const payload={distance:Number($('distance').value),runningTime:seconds,runningDate:$('running-date').value,content:$('content').value};
    const editing=state.editId!==null;runBusy(true);errorAt('run-error','');
    try {
      const saved=await api(editing?`/api/runs/${state.editId}`:'/api/runs',{method:editing?'PUT':'POST',body:payload});
      if(!state.user)return;
      state.runs=editing?state.runs.map(r=>r.id===saved.id?saved:r):[saved,...state.runs];
      state.runs.sort((a,b)=>b.runningDate.localeCompare(a.runningDate)||b.id-a.id);state.loaded=true;
      $('run-dialog').close();render();toast(editing?'기록을 수정했습니다.':'오늘의 러닝을 기록했습니다.');
    } catch(error) {if(state.user) errorAt('run-error',error.message);}
    finally {runBusy(false);}
  };
  $('records').onclick=event=>{
    const button=event.target.closest('button[data-id]'); if(!button) return;
    const run=state.runs.find(r=>r.id===Number(button.dataset.id));if(!run)return;
    if(button.dataset.action==='edit') openRun(run);
    else {state.deleteId=run.id;$('delete-description').textContent=`${run.runningDate} · ${distance(run.distance)}km`;errorAt('delete-error','');$('delete-dialog').showModal();}
  };
  $('confirm-delete').onclick=async()=>{
    if($('confirm-delete').disabled||state.deleteId===null)return;
    const id=state.deleteId;$('confirm-delete').disabled=true;$('confirm-delete').textContent='삭제 중…';errorAt('delete-error','');
    $('delete-dialog').querySelectorAll('[data-close]').forEach(button=>{button.disabled=true;});
    try {await api(`/api/runs/${id}`,{method:'DELETE'});if(!state.user)return;state.runs=state.runs.filter(r=>r.id!==id);state.deleteId=null;$('delete-dialog').close();render();toast('기록을 삭제했습니다.');}
    catch(error) {if(state.user)errorAt('delete-error',error.message);}
    finally {$('confirm-delete').disabled=false;$('confirm-delete').textContent='삭제';$('delete-dialog').querySelectorAll('[data-close]').forEach(button=>{button.disabled=false;});}
  };
  async function restoreSession() {
    if(!state.token)return;
    authBusy(true);
    try {state.user=await api('/api/auth/me');showApp();await loadRuns();}
    catch(error) {setToken('');errorAt('auth-error',error.message);}
    finally {authBusy(false);}
  }
  restoreSession();
})();
