import requests 
import json 
import pytest 

from helpers import create_tree 
from helpers import create_user_header 
from helpers import create_skill 
from helpers import create_achievement 

def test_created_on_tree_creation(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    
    # getting a trees layout requires finding the orientation so this requests
    # proves the existance of the auto generated blank orientation
    res = requests.get(
        f"{base_url}/api/trees/me/layout/{tree['id']}",
        headers=header 
    )
    res.raise_for_status()
    print(res.json())

def test_skills_add_on_creation(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    skill = create_skill(tree, header) 
    
    res = requests.get(
        f"{base_url}/api/trees/me/layout/{tree['id']}",
        headers=header 
    )
    res.raise_for_status()
    assert res.json()['skillLayout'][skill['id']]['name'] == skill['name']
    

def test_achievements_add_on_creation(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    achievement = create_achievement(tree, header) 
    res = requests.get(
        f"{base_url}/api/trees/me/layout/{tree['id']}",
        headers=header 
    )
    res.raise_for_status()
    assert res.json()['achievementLayout'][achievement['id']]['title'] == achievement['title']
    
def test_patch_orientation(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header)
    achievement = create_achievement(tree, header)
    skill = create_skill(tree, header)
    
    # No content in response
    requests.patch(
        f"{base_url}/api/orientations/me/{tree['id']}",
        json=[{"type": "SKILL", "id": skill['id'], "x": 0.2, "y": 0.8},
              {"type": "ACHIEVEMENT", "id": achievement['id'], "x": 0.8, "y": 0.2}],
        headers=header 
    )
    
    layout_res = requests.get(
        f"{base_url}/api/trees/me/layout/{tree['id']}",
        headers=header 
    )
    layout_res.raise_for_status()
    assert layout_res.json()['skillLayout'][skill['id']]['x'] == 0.2
    assert layout_res.json()['skillLayout'][skill['id']]['y'] == 0.8
    assert layout_res.json()['achievementLayout'][achievement['id']]['x'] == 0.8
    assert layout_res.json()['achievementLayout'][achievement['id']]['y'] == 0.2