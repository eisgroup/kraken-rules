const header = `/*
 *  Copyright ${new Date().getFullYear()} EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
`
var fs = require('fs');
var path = require('path');
var walk = function (dir, done) {
    var results = [];
    fs.readdir(dir, function (err, list) {
        if (err) return done(err);
        var pending = list.length;
        if (!pending) return done(null, results);
        list
            .forEach(function (file) {
                file = path.resolve(dir, file);
                if (!fs.statSync(file).isDirectory() && !file.endsWith(".json") && !file.endsWith(".snap") && !file.endsWith("_SanityEntryPointNames.d.ts")) {
                    const content = fs.readFileSync(file, { encoding: "UTF-8" })
                    if (content.indexOf("Licensed under the Apache License, Version 2.0") === -1) {
                        console.log("Adding header to file: " + file);
                        fs.writeFileSync(file,
                            `${header}
${content}`)
                    }
                }
                fs.stat(file, function (err, stat) {
                    if (stat && stat.isDirectory()) {
                        walk(file, function (err, res) {
                            results = results.concat(res);
                            if (!--pending) done(null, results);
                        });
                    } else {
                        results.push(file);
                        if (!--pending) done(null, results);
                    }
                });
            });
    });
};

const args = process.argv.slice(2);
if (!args.length) {
    throw new Error("Please provide directory to add licence headers")
}

console.log("Running script to add licence header:\n");
console.log(header);

args.forEach(dir => {
    console.log(`Adding licence headers to directory: '${dir}'`);
    return walk(dir, () => { });
})
