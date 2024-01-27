import argparse
from constants import ALL_MAPS
import random
import os
import json

GEN_MAP_DIR = os.path.join(os.getcwd(), 'generated_map_list')
def main():
    parser = argparse.ArgumentParser(description='Generates list of 10 maps, stores them inside maps_list/')

    parser.add_argument('num_maps', nargs='?', type=int, default=10, help='Number of maps you want to generate')
    args = parser.parse_args()

    numMaps = args.num_maps

    if numMaps > 10:
        print(f"ERROR: max number of maps allowed is 10! You've inputted {numMaps} maps")
        return
    randomMapsList = random.sample(ALL_MAPS,numMaps)

    os.makedirs(GEN_MAP_DIR, exist_ok=True)

    fileList = os.listdir(GEN_MAP_DIR)
    numExistingMaps = len(fileList)

    newFileName = f"random_maps_{numExistingMaps}.json"

    fullFilePath = os.path.join(GEN_MAP_DIR,newFileName)

    with open(fullFilePath, 'w') as outputJsonFile:
        json.dump(randomMapsList, outputJsonFile)

    print(f'List of randomly generated maps has been written to: generated_map_list/{newFileName}')


if __name__ == '__main__':
    main()