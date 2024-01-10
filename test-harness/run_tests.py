import subprocess
import multiprocessing
def runSampleProgram(i):
    command = ["echo", f"num is {i}"]
    process = subprocess.Popen(command, stdout=subprocess.PIPE,stderr=subprocess.PIPE, text=True )
    output, error = process.communicate()
    print(f"output is: {output}")
    return f"my output is: {output}, error is: {error}"

if __name__ == '__main__':
    nums = [0,1,2,3,4]
    with multiprocessing.Pool() as pool:
        results = pool.map(runSampleProgram, nums)

    print(results)
