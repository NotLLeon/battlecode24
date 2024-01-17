import json

def getTeamNameToIDDict():
    try:
        with open("unranked_accepted_teams_id.json", 'r') as json_file:
            unrankedTeamsDict = json.load(json_file)
            idToTeamName = {}

            # Since this is a bijection, want the reverse mapping too
            for teamName in unrankedTeamsDict:
                idToTeamName[unrankedTeamsDict[teamName]] = teamName
            return unrankedTeamsDict, idToTeamName
    except FileNotFoundError as e:
        print(f"Error: {e}")

def getJWTToken():
    try:
        with open('jwt.txt', 'r') as file:
            jwtToken = file.readline().strip()
            return jwtToken
    except FileNotFoundError as e:
        print(f"Error: {e}")


def loadJSON(filePath):
    try:
        with open(filePath, 'r') as json_file:
            loadedJSON = json.load(json_file)
            return loadedJSON
    except FileNotFoundError as e:
        print(f"File not found: {e}")
    except json.JSONDecodeError as e:
        print(f"File not a valid JSON: {e}")
