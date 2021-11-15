/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.facade;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import kraken.model.EntryPointName;
import kraken.model.Rule;
import kraken.namespace.Namespaced;
import kraken.service.ReloadableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class DynamicRulesFacade {

    @Autowired
    private ReloadableRepository reloadableRepository;

    @ApiOperation(
            value = "Add new rules in DSL format to entry point by name",
            response = ResponseEntity.class,
            notes = "Endpoint for creating rules and adding them to entry point by name. " +
                    "Request body will accept DSL with rules. " +
                    "Entry point name path param will accept only predefined entry point names: " +
                    "QA1, QA2, QA3, QA4, QA5."
    )
    @PostMapping("/dynamic/rule/dsl/{entryPointName}")
    public ResponseEntity<String> addDSLRule(
            @ApiParam(required = true, value = "DSL with rules")
            @RequestBody String dslRuleList,
            @ApiParam(required = true, value = "Predefined entry point name")
            @PathVariable String entryPointName
    ){
        return reloadableRepository
                .addRules(EntryPointName.valueOf(entryPointName), dslRuleList)
                .createResponseEntity();
    }

    @ApiOperation(
            value = "Get all rules from entry point by name",
            response = List.class,
            notes = "Endpoint for getting all rule that belongs to entry point by name. " +
                    "Entry point name path param will accept only predefined entry point names: " +
                    "QA1, QA2, QA3, QA4, QA5."
    )
    @GetMapping("/dynamic/rule/{entryPointName}")
    public List<Rule> getEntryPointRules(
            @ApiParam(required = true, value = "Predefined entry point name")
            @PathVariable String entryPointName
    ){
        final Map<String, Rule> rules = reloadableRepository
                .getRules()
                .stream()
                .collect(Collectors.toMap(Rule::getName, x -> x));
        return reloadableRepository
                .getEntryPoint(EntryPointName.valueOf(entryPointName))
                .getRuleNames()
                .stream()
                .map(rules::get)
                .collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Remove rule from entry point by name",
            response = ResponseEntity.class,
            notes = "Endpoint should be used to remove rule name from entry point by name. " +
                    "Rule name path param will expect exiting rule name on entry point. " +
                    "Entry point name path param will accept only predefined entry point names: " +
                    "QA1, QA2, QA3, QA4, QA5."
    )
    @DeleteMapping("/dynamic/rule/{ruleName}/entrypoint/{entryPointName}")
    public ResponseEntity<String> removeRule(
            @ApiParam(required = true, value = "Existing rule name on entry point")
            @PathVariable String ruleName,
            @ApiParam(required = true, value = "Predefined entry point name")
            @PathVariable String entryPointName
    ){
        return reloadableRepository
                .removeRules(EntryPointName.valueOf(entryPointName), List.of(ruleName))
                .createResponseEntity();
    }

    @ApiOperation(
            value = "Get all existing rules",
            response = Collection.class,
            notes = "Endpoint for getting all existing rule. " +
                    "Return Collection of rules."
    )
    @GetMapping("/dynamic/rules/all")
    public Collection<Rule> getAllRules(){
        return reloadableRepository.getRules();
    }

    @ApiOperation(
            value = "Remove all rules from entry point by name",
            response = ResponseEntity.class,
            notes = "Endpoint should be used to remove all rules from entry point by given entry point name. " +
                    "Entry point name path param will accept only predefined entry point names: " +
                    "QA1, QA2, QA3, QA4, QA5."
    )
    @PostMapping("/dynamic/entrypoint/{entryPointName}/reset")
    public void resetEntryPoint(
            @ApiParam(required = true, value = "Predefined entry point name")
            @PathVariable String entryPointName
    ){
        reloadableRepository.clear(EntryPointName.valueOf(entryPointName));
    }

    @ApiOperation(
            value = "Remove all rules from runtime repository and cleans dynamic EntryPoints content",
            response = ResponseEntity.class
    )
    @DeleteMapping("/dynamic/rules/clear")
    public void cleanApp() {
        reloadableRepository.clear();
    }

}
