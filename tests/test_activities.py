import requests 
import pytest 
import json 

from helpers import create_skill 
from helpers import create_activity_full
from helpers import create_tree 
from helpers import create_user_header 

def test_create_activity(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    activity = create_activity_full(tree, header) 
    
    required_keys = ["id", "name", "description", "duration", "weightedSkills"]
    for key in required_keys:
        assert key in activity 
    assert activity['id'] != None 
    assert activity['name'] != None 
    assert activity['duration'] >= 0
    assert activity['duration'] <= 12 
    assert activity['weightedSkills'] != None 
    assert len(activity['weightedSkills']) > 0

def test_create_activity_not_authed(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.post(
            f"{base_url}/api/activities/me",
            json={
                'name': 'activity name',
                'description': 'activity description',
                'duration': 2,
                'skillWeights': [{"skillId": "60c72b2f9b1d8b1c8a4f3b2e", "weight": 1}]
            },
            headers={}
        )
        res.raise_for_status()

def test_get_authed_user_activities(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    activities = set()
    for i in range(3):
        activities.add(create_activity_full(tree, header)['id'])
    
    res = requests.get(
        f"{base_url}/api/activities/me",
        headers = header
    )
    res.raise_for_status()
    assert res.json() != None 
    
    actual = {activity['id'] for activity in res.json()}
    
    assert activities == actual
    
def test_get_authed_user_activity(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    activity = create_activity_full(tree, header) 
    
    res = requests.get(
        f"{base_url}/api/activities/me/{activity['id']}",
        headers=header 
    )
    res.raise_for_status() 
    
    assert res.json() != None 
    
    assert activity == res.json() 
    
def test_get_authed_user_activity_dne(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/activities/me/60c72b2f9b1d8b1c8a4f3b2e",
            headers=header 
        )
        res.raise_for_status()
        
def test_get_authed_user_activity_wrong_user(base_url, user_header):
    user1, header1 = user_header 
    user2, header2 = create_user_header() 
    
    tree1 = create_tree(header1) 
    tree2 = create_tree(header2) 
    
    activity = create_activity_full(tree1, header1) 
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/activities/me/{activity['id']}",
            headers=header2 
        )
        res.raise_for_status() 
        
def test_patch_activity(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header)
    activity = create_activity_full(tree, header) 
    
    new_duration = 1.5
    res = requests.patch(
        f"{base_url}/api/activities/me/{activity['id']}",
        json={"duration": new_duration},
        headers=header 
    )
    res.raise_for_status() 
    assert res.json() != None 
    updated = res.json() 
    assert "duration" in updated 
    assert updated["duration"] == new_duration 
    
def test_patch_activity_invalid_field(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    activity = create_activity_full(tree, header) 
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.patch(
            f"{base_url}/api/activites/me/{activity['id']}",
            json={"invalid_field": "dnm"},
            headers=header 
        )
        res.raise_for_status()

def test_delete_activity(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    activity = create_activity_full(tree, header) 
    
    delete_res = requests.delete(
        f"{base_url}/api/activites/me/{activity['id']}",
        headers=header 
    )
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/activites/me/{activity['id']}",
            headers=header 
        )
        res.raise_for_status()
    