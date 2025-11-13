import requests
import pytest 
import json

from helpers import create_skill
from helpers import create_tree
from helpers import create_user_header

def test_create_skill(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    skill = create_skill(tree, header)
    
    required_keys = ["id", "treeId", "name", "backgroundUrl", "timeSpentHours", "parentSkillId"]
    for key in required_keys:
        assert key in skill
    assert skill['id'] != None
    assert skill['treeId'] != None
    assert skill['name'] != None
    assert skill['timeSpentHours'] >= 0
    
def test_create_skill_not_authed(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.post(
            f"{base_url}/api/skills/me",
            json={
                'treeId': tree['id'],
                'name': 'skill name',
                'backgroundUrl': None,
                'timeSpentHours': 0,
                'parentSkillId': None
            },
            headers={}
        )
        res.raise_for_status()

def test_get_authed_user_skills(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    skill1 = create_skill(tree, header)
    skill2 = create_skill(tree, header)
    skill3 = create_skill(tree, header)
    
    res = requests.get(
        f"{base_url}/api/skills/me",
        headers = header
    )
    
    res.raise_for_status()
    assert res.json() != None
    
    expected = {skill1['id'], skill2['id'], skill3['id']}
    actual = {skill['id'] for skill in res.json()}
    
    assert expected == actual
    
def test_get_authed_user_skill(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    skill = create_skill(tree, header)
    
    res = requests.get(
        f"{base_url}/api/skills/me/{skill['id']}",
        headers=header
    )
    
    res.raise_for_status()
    assert res.json() != None
    
    expected = skill
    actual = res.json()
    
    assert expected == actual
    
def test_get_authed_user_skill_dne(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/skills/me/60c72b2f9b1d8b1c8a4f3b2e",
            headers=header
        )
        res.raise_for_status()

def test_get_authed_user_skill_wrong_user(base_url, user_header):
    user1, header1 = user_header
    user2, header2 = create_user_header()
    
    tree1 = create_tree(header1)
    tree2 = create_tree(header2)
    
    skill = create_skill(tree1, header1)
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/skills/me/{skill['id']}",
            headers=header2
        )
        res.raise_for_status()

def test_patch_skill(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    skill = create_skill(tree, header)
    
    new_name = "new skill name"
    res = requests.patch(
        f"{base_url}/api/skills/me/{skill['id']}",
        json={"name": new_name},
        headers=header
    )
    
    res.raise_for_status()
    assert res.json() != None
    
    updated = res.json()
    assert "name" in updated
    assert updated['name'] == new_name

def test_patch_skill_invalid_field(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    skill = create_skill(tree, header)
    
    invalid_patch = {"invalid_field": "dnm"}
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.patch(
            f"{base_url}/api/skills/me/{skill['id']}",
            json=invalid_patch,
            headers=header
        )
        
        res.raise_for_status()

# get by id must be passing for this test to be considered valid
def test_delete_skill(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    skill = create_skill(tree, header)
    
    delete_res = requests.delete(
        f"{base_url}/api/skills/me/{skill['id']}",
        headers=header
    )
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/skills/me/{skill['id']}",
            headers=header
        )
        res.raise_for_status()
    
    
