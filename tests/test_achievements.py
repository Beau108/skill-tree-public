import requests
import pytest 
import json 

from helpers import create_achievement 
from helpers import create_tree 
from helpers import create_user_header  

def test_create_achievement(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header)
    achievement = create_achievement(tree, header)
    assert 'id' in achievement
    assert achievement['id'] != None
    assert 'treeId' in achievement 
    assert 'title' in achievement 
    assert 'backgroundUrl' in achievement 
    assert 'prerequisites' in achievement 
    
def test_create_achievement_not_authed(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.post(
            f"{base_url}/api/achievements/me",
            json={
                'treeId': tree['id'],
                'name': 'achievement name',
                'backgroundUrl': None,
                'timeSpentHours': 0,
                'parentSkillId': None
            },
            headers={}
        )
        res.raise_for_status()
        
def test_get_authed_user_achievements(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header)
    ach1 = create_achievement(tree, header)
    ach2 = create_achievement(tree, header)
    ach3 = create_achievement(tree, header)
    
    res = requests.get(
        f"{base_url}/api/achievements/me",
        headers = header
    )
    
    res.raise_for_status()
    assert res.json() != None 
    
    expected = {ach1['id'], ach2['id'], ach3['id']}
    actual = {achievement['id'] for achievement in res.json()}
    assert expected == actual
    
def test_get_authed_user_achievement(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    achievement = create_achievement(tree, header) 
    
    res = requests.get(
        f"{base_url}/api/achievements/me/{achievement['id']}",
        headers=header
    )
    
    res.raise_for_status()
    assert res.json() != None 
    
    expected = achievement 
    actual = res.json() 
    assert expected==actual 
    
def test_get_authed_user_achievement_dne(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/achievements/me/60c72b2f9b1d8b1c8a4f3b2e",
            headers=header
        )
        res.raise_for_status()
        
def test_get_authed_user_achievement_wrong_user(base_url, user_header):
    user1, header1 = user_header 
    user2, header2 = create_user_header() 
    
    tree1 = create_tree(header1)
    tree2 = create_tree(header2) 
    
    achievement = create_achievement(tree1, header1) 
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/achievements/me/{achievement['id']}",
            headers=header2
        )
        res.raise_for_status()
        
def test_patch_achievement(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    achievement = create_achievement(tree, header) 
    
    new_title = "new achievement title"
    res = requests.patch(
        f"{base_url}/api/achievements/me/{achievement['id']}",
        json={"title":new_title},
        headers=header
    )
    
    res.raise_for_status()
    assert res.json() != None 
    updated = res.json() 
    assert 'title' in updated 
    assert updated['title'] == new_title
    
def test_patch_achievement_invalid_field(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    achievement = create_achievement(tree, header) 
    
    invalid_patch = {"invalid_field": "dnm"}
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.patch(
            f"{base_url}/api/achievements/me/{achievement['id']}",
            json=invalid_patch,
            headers=header
        )
        
        res.raise_for_status()
    
def test_delete_achievement(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    achievement = create_achievement(tree, header) 
    
    delete_res = requests.delete(
        f"{base_url}/api/achievements/me/{achievement['id']}",
        headers=header
    )
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/achievements/me/{achievement['id']}",
            headers=header 
        )
        res.raise_for_status()