from datetime import datetime
from enum import Enum




def getDateTimeToNearestSecond(dateTimeStrFull):
    parsed_datetime = datetime.fromisoformat(dateTimeStrFull)

    # Round to the nearest second, and this rounds down!
    rounded_datetime = parsed_datetime.replace(microsecond=0)

    rounded_datetime_str = rounded_datetime.strftime('%Y-%m-%dT%H:%M:%S')
    return rounded_datetime_str


# Datetime string
datetime_str_input = '2024-01-14T03:11:37.158006-05:00'
print(getDateTimeToNearestSecond(datetime_str_input))
