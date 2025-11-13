import pytest
import requests
import random

from helpers import create_user_header

BASE_URL = "http://localhost:8080"

first = ["Big", "Small", "Build", "Best", "OP", "Cool", "Epic", "Amazing", "Ok", "Splended", "Blended", "Clean", "Up", "Down", "Left", "Right", "Baller"]
last = ["Champ", "Goat", "Master", "Guy", "Bro", "Shake", "Pipe", "Twist", "Tooth", "Yolk", "Egg", "Shell", "Gas", "Wind", "Fella", "Turtle", "Dog", "Cat"]

def auth_headers(auth_token):
    return {
        "Authorization": f"Bearer {auth_token}"
    }
    
@pytest.fixture
def user_header():
    return create_user_header()
    
    
@pytest.fixture(scope="session")
def base_url():
    return BASE_URL