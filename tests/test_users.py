import requests
import pytest
import json

def test_create_new_user(base_url, user_header):
    user, header = user_header
    
    assert "displayName" in user
    assert len(user["displayName"]) >= 3
    
    assert "profilePictureUrl" in user

def test_create_new_user_already_exists(base_url, user_header):
    user, header = user_header
    res = requests.post(f"{base_url}/api/users/me", json=user, headers=header)
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.post(
            f"{base_url}/api/users/me",
            json=user,
            headers=header
        )
        res.raise_for_status()

def test_create_new_user_not_authed(base_url):
    user = {"displayName" : "MagicUnicorn85", "email": "eugene@kkrab.com", "profilePictureUrl": None}
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.post(
            f"{base_url}/api/users/me", 
            json=user, 
            headers={})
        res.raise_for_status()
    
def test_get_authed_user(base_url, user_header):
    user, header = user_header
    
    res = requests.get(
        f"{base_url}/api/users/me", 
        headers = header)
    assert res.json() == user
    
def test_get_authed_user_not_authed(base_url, user_header):
    user, header = user_header
    header = {}
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/users/me",
            json=user,
            headers=header
        )
        res.raise_for_status()
    
def test_patch_user(base_url, user_header):
    user, header = user_header
    header["Content-Type"] = "application/merge-patch+json"
    new_name = f"{user['displayName'][::-1]}"
    res = requests.patch(
        f"{base_url}/api/users/me", 
        json={"displayName":new_name}, 
        headers=header
    )
    
    res.raise_for_status()
    assert res.json() != None
    
    updated = res.json()
    print(updated)
    assert "displayName" in updated
    assert updated["displayName"]==new_name
    
def test_patch_user_invalid_field(base_url, user_header):
    user, header = user_header
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.patch(
            f"{base_url}/api/users/me",
            json={"invalidField":"lalalalalala"},
            headers=header
        )
        res.raise_for_status()
    