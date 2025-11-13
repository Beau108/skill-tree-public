import requests 
import pytest 
import json 

from helpers import create_user_header 
from helpers import create_tree 
from helpers import create_activity_full 
from helpers import create_skill 

def test_request(base_url, user_header):
    user, header = user_header 
    user2, header2 = create_user_header()
    
    res_request = requests.post(
        f"{base_url}/api/friendships/me/{user2['displayName']}",
        headers=header 
    )
    
    assert res_request.text == user2['displayName']
    
def test_accept(base_url, user_header):
    user, header = user_header 
    user2, header2 = create_user_header() 
    
    requests.post(
        f"{base_url}/api/friendships/me/{user2['displayName']}",
        headers=header 
    )
    
    res_friends_list = requests.get(
        f"{base_url}/api/friendships/me",
        headers=header2
    )
    
    friendship_id = res_friends_list.json()['incoming'][0]['friendshipId']
    
    res = requests.patch(
        f"{base_url}/api/friendships/me/{friendship_id}",
        params={"status": "ACCEPTED"},
        headers=header2
    )
    
    res.raise_for_status()
    print(res.text)
    assert res.status_code == 200