import requests
import pytest 
import json 

from helpers import create_tree 
from helpers import create_user_header

def test_create_tree(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    
    assert 'id' in tree 
    assert tree['id'] != None
    assert 'name' in tree 
    assert tree['name'] != None 
    assert 'backgroundUrl' in tree 
    assert 'description' in tree 
    assert 'visibility' in tree 
    assert tree['visibility'] != None
    
def test_create_tree_not_authed(base_url, user_header):
    user, header = user_header 
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.post(
            f"{base_url}/api/trees/me",
            json={
                'name': "tree name",
                'backgroundUrl': None,
                'description': 'tree description',
                'visibility': "PRIVATE"
            },
            headers={}
        )
        res.raise_for_status()
        
def test_get_user_trees(base_url, user_header):
    user, header = user_header 
    tree1 = create_tree(header)
    tree2 = create_tree(header)
    tree3 = create_tree(header) 
    
    print(tree1)
    res = requests.get(
        f"{base_url}/api/trees/me",
        headers=header
    )
    
    res.raise_for_status()
    assert res.json() != None 
    
    expected = {tree1['id'], tree2['id'], tree3['id']}
    actual = {skill['id'] for skill in res.json()}
    assert expected == actual
    
def test_get_user_tree(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    
    res = requests.get(
        f"{base_url}/api/trees/me/{tree['id']}",
        headers=header
    )
    
    res.raise_for_status()
    
    assert res.json() != None 
    assert tree == res.json()
    
def test_get_user_tree_dne(base_url, user_header):
    user, header = user_header 
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/trees/me/60c72b2f9b1d8b1c8a4f3b2e",
            headers=header
        )
        res.raise_for_status()
        
def test_get_user_tree_wrong_user(base_url, user_header):
    user1, header1 = user_header 
    user2, header2 = create_user_header() 
    
    tree = create_tree(header1)
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/trees/me/{tree['id']}",
            headers=header2
        )
        res.raise_for_status()

def test_patch_tree(base_url, user_header):
    user, header = user_header
    tree = create_tree(header)
    
    new_description = "This is the new description"
    res = requests.patch(
        f"{base_url}/api/trees/me/{tree['id']}",
        json={"description": new_description},
        headers=header 
    )
    res.raise_for_status()
    
    assert res.json() != None 
    assert res.json()['description'] == new_description 
    assert res.json()['id'] == tree['id']

def test_patch_tree_invalid_field(base_url, user_header):
    user, header = user_header 
    tree = create_tree(header) 
    
    invalid_patch = {"invalidField": "dnm"}
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.patch(
            f"{base_url}/api/trees/me/{tree['id']}",
            json=invalid_patch,
            headers=header 
        )
        res.raise_for_status()

def test_delete_tree(base_url, user_header):
    user, header = user_header
    tree = create_tree(header) 
    
    delete_res = requests.delete(
        f"{base_url}/api/trees/me/{tree['id']}",
        headers=header
    )
    delete_res.raise_for_status()
    
    with pytest.raises(requests.exceptions.HTTPError):
        res = requests.get(
            f"{base_url}/api/trees/me/{tree['id']}",
            headers=header
        )
        res.raise_for_status()
    
    
    