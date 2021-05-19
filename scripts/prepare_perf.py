import json
import sys

command = ''
with open(sys.argv[1]) as workflow_json:
    data = json.load(workflow_json)
    for p in data['processes']:
    	executor = p['config']['executor']
    	command += 'sudo perf stat -o perf_results.txt --append ./' + executor['executable'] + ' ' + ' '.join(executor['args']) + '\n'

with open('perf.sh', 'w+') as f:
	f.write(command)