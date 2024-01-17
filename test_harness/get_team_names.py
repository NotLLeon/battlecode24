import json
import requests


def update_unranked_accepting_teams_json():
    startURL = 'https://api.battlecode.org/api/team/bc24/t/?ordering=-rating%2Cname&search='

    currURL = startURL
    myDict = {}
    while currURL:
        response = requests.get(currURL)
        json_data = response.json()

        for teamInfo in json_data['results']:
            if teamInfo['profile']['auto_accept_unranked'] == True:
                myDict[teamInfo['name']] = teamInfo['id']
        currURL = json_data['next']

    with open("unranked_accepted_teams_id.json", "w") as json_file:
        json.dump(myDict, json_file, indent=2)