#!/usr/bin/env python3
"""실제 HTTP 서버 검증. 전용 테스트 DB에만 실행하세요. 생성한 계정은 테스트 DB에 남습니다."""
import base64, json, os, time, urllib.error, urllib.request, uuid, sys
base=os.environ.get('RUNSHOT_TEST_API_URL','http://127.0.0.1:8080').rstrip('/')
gateway=os.environ.get('RUNSHOT_GATEWAY_KEY','')
checks=[]
def request(method,path,body=None,token=None,expected=200):
    headers={'Content-Type':'application/json'}
    if gateway: headers['X-Runshot-Gateway-Key']=gateway
    if token: headers['Authorization']='Bearer '+token
    payload=None if body is None else json.dumps(body).encode()
    req=urllib.request.Request(base+path,data=payload,headers=headers,method=method)
    try:
        with urllib.request.urlopen(req,timeout=15) as response: code=response.status; raw=response.read()
    except urllib.error.HTTPError as error: code=error.code; raw=error.read()
    if code!=expected: raise AssertionError(f'{method} {path}: expected {expected}, got {code}: {raw[:500]!r}')
    checks.append({'method':method,'path':path,'status':code})
    return json.loads(raw) if raw else None
stamp=uuid.uuid4().hex[:12]
password=base64.urlsafe_b64encode(os.urandom(24)).decode()
email=f'qa-{stamp}@example.invalid'
second=f'qa-other-{stamp}@example.invalid'
request('GET','/api/runs',expected=401)
request('POST','/api/users',expected=400)
user=request('POST','/api/users',{'email':email,'password':password,'nickname':'검증 계정'},expected=201)
assert 'password' not in user
request('POST','/api/users',{'email':email,'password':password,'nickname':'중복'},expected=409)
request('POST','/api/auth/login',{'email':email,'password':'wrong-password'},expected=401)
login=request('POST','/api/auth/login',{'email':email,'password':password})
token=login['accessToken']
assert request('GET','/api/auth/me',token=token)['id']==user['id']
assert request('GET','/api/runs',token=token)==[]
record={'distance':6.5,'runningTime':2400,'runningDate':'2026-01-01','content':'실제 DB 검증 <script>alert(1)</script>'}
run=request('POST','/api/runs',record,token,201)
assert run['userId']==user['id'] and run['pace']==369 and 'password' not in run
assert request('GET','/api/runs',token=token)[0]['id']==run['id']
request('PUT',f"/api/runs/{run['id']}",{},token,400)
request('POST','/api/runs',dict(record,runningDate='2099-01-01'),token,400)
request('POST','/api/users',{'email':second,'password':password,'nickname':'다른 검증 계정'},expected=201)
other=request('POST','/api/auth/login',{'email':second,'password':password})['accessToken']
assert request('GET','/api/runs',token=other)==[]
request('GET',f"/api/runs/users/{user['id']}",token=other,expected=403)
for method,body in [('GET',None),('PUT',record),('DELETE',None)]:
    request(method,f"/api/runs/{run['id']}",body,other,404)
request('POST','/api/runs',dict(record,userId=user['id']),other,403)
updated=request('PUT',f"/api/runs/{run['id']}",dict(record,distance=8.0),token)
assert updated['pace']==300
assert request('GET',f"/api/runs/{run['id']}",token=token)['distance']==8.0
request('GET','/api/runs',token='invalid.jwt.token',expected=401)
request('DELETE',f"/api/runs/{run['id']}",token=token,expected=204)
request('GET',f"/api/runs/{run['id']}",token=token,expected=404)
assert request('GET','/api/runs',token=token)==[]
print(json.dumps({'passed':True,'requests':len(checks),'checks':checks},ensure_ascii=False,indent=2))
