import sys
import os


def main():
    args = sys.argv

    if len(args) != 3:
        print("Usage: ./{} <source directory> <destination directory>")
        print("Usage: ./{} initialBot nextBot")
        return

    source = args[1]
    destination = args[2]

    print("Copying {} to {}".format(source, destination))
    # copy all contents in the folders
    os.system(f"cd src && cp -a {source}/. {destination}/")

    # replace all instances of source directory references in the destination directory
    os.system(f"cd src/{destination} && find . -name '*.java' -exec sed -i '' -e 's/{source}/{destination}/g' {{}} +")

if __name__ == "__main__":
    main()