import requests

url = "https://api.battlecode.org/api/compete/bc24/request/"
headers = {"authority": "api.battlecode.org",
           "accept": "application/json, text/javascript, */*; q=0.01",
           "accept-language": "en-US,en;q=0.9",
           "authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzA1NDUwODgxLCJpYXQiOjE3MDUwMTg4ODEsImp0aSI6IjRiNWQ2NjY1ODhkYzQ5ZmJhN2RjNzRlYTVkMTBkYjcwIiwidXNlcl9pZCI6MTg4N30.TRyS00m5Iao50QZVVaBJHeK4Pgdar20Q8E7dlvc1mJs",
           "content-type": "application/json",
           "origin": "https://play.battlecode.org",
           "referer": "https://play.battlecode.org/",
           "sec-fetch-mode": "cors",
           "sec-fetch-site": "same-site",
           "user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"}
data = {"is_ranked":False,"requested_to":508,"player_order":"?","map_names":["DefaultMedium", "DefaultSmall", "DefaultHuge", "DefaultLarge"]}

# Player order: + means Requester first, - means requester last, ? means alternating
response = requests.post(url, headers=headers, json=data)

# Print the response status code and content
print(f"Status Code: {response.status_code}")
print("Response Content:", response.text)