const { exec } = require('child_process');

const createBranch = () => {
    if (!process.argv[2]) {
        throw new Error('Failed to create branch. No name is provided')
    }
    console.log('Creating branch with name ' + process.argv[2]);
    exec('git checkout -b ' + process.argv[2], (err, stdout, stderr) => {
        if (err) {
            return;
        }
        console.log(stdout);
        checkBranch()
    });
}

const checkBranch = () => {
    exec('git branch', (err, stdout, stderr) => {
        if (err) {
            return;
        }
        const branchNameWithAsterisk = stdout.split('\n').filter(x => x.indexOf('*') == 0)[0]
        const branchName = branchNameWithAsterisk.slice(2, branchNameWithAsterisk.length)
        pushToOrigin(branchName)
    });
}
const pushToOrigin = (branchName) => {
    exec('git push -u origin ' + branchName, (err, stdout, stderr) => {
        console.log(`Pushing current branch "${branchName}" to origin`);
        if (err) {
            return;
        }
        console.log(stdout);
    });
}

createBranch()
return 0