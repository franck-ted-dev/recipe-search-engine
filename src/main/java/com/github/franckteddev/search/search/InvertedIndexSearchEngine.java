package com.github.franckteddev.search.search;

import com.github.franckteddev.search.model.IngredientCompleted;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.model.RecipeCompleted;
import com.github.franckteddev.search.utility.Converter;
import com.github.franckteddev.search.utility.Normalizer;

import java.util.*;
import java.util.stream.Collectors;

public class InvertedIndexSearchEngine implements SearchEngine{
    private final Map<String, Set<Integer>> invertedIndex;
    private final List<RecipeCompleted> recipesCompleted;
    private final AllStrategy allStrategy;
    private final AnyStrategy anyStrategy;
    private final NoneStrategy noneStrategy;

    public InvertedIndexSearchEngine(List<RecipeCompleted> recipesCompleted){
        this.allStrategy = new AllStrategy();
        this.anyStrategy = new AnyStrategy();
        this.noneStrategy = new NoneStrategy();
        this.invertedIndex = new HashMap<>();
        this.recipesCompleted = recipesCompleted;
        int pos = 0;

        for (RecipeCompleted recipeCompleted : recipesCompleted) {
            List<IngredientCompleted> ingredientsCompleted = recipeCompleted.ingredientsCompleted();
            for (IngredientCompleted ingredientCompleted : ingredientsCompleted){
                String canonicalName = ingredientCompleted.canonicalName();
                if(canonicalName == null){
                    continue;
                }else {
                    canonicalName = Normalizer.normalize(canonicalName);
                }
                invertedIndex.computeIfAbsent(canonicalName, k -> new HashSet<>()).add(pos);
            }
            pos++;
        }
    }

    public boolean isReady(){
        return !recipesCompleted.isEmpty() && !invertedIndex.isEmpty();
    }

    @Override
    public List<Recipe> search(SearchRequestParam query) {
        List<String> all = query.all();
        List<String> any = query.any();
        List<String> none = query.none();

        List<Set<Integer>> list = new ArrayList<>();

        if(!all.isEmpty()){
            List<Set<Integer>> positionsList1 = new ArrayList<>();
            for(String canonicalName: all){
                positionsList1.add(this.search(canonicalName));
            }
            Set<Integer> positionsAfterAllStrategy = this.allStrategy.executeStrategy(positionsList1, this.recipesCompleted.size());
            list.add(positionsAfterAllStrategy);
        }

        if(!any.isEmpty()){
            List<Set<Integer>> positionsList2 = new ArrayList<>();
            for(String canonicalName: any){
                positionsList2.add(this.search(canonicalName));
            }
            Set<Integer> positionsAfterAnyStrategy = this.anyStrategy.executeStrategy(positionsList2, this.recipesCompleted.size());
            list.add(positionsAfterAnyStrategy);
        }

        if(!none.isEmpty()){
            List<Set<Integer>> positionsList3 = new ArrayList<>();
            for(String canonicalName: none){
                positionsList3.add(this.search(canonicalName));
            }
            Set<Integer> positionsAfterNoneStrategy = this.noneStrategy.executeStrategy(positionsList3, this.recipesCompleted.size());
            list.add(positionsAfterNoneStrategy);
        }

        return this.intersect(list).stream()
                .map(recipesCompleted::get)
                .map(Converter::convertToRecipe)
                .sorted(Comparator.comparing(Recipe::name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private Set<Integer> search(String canonicalName){
        return new HashSet<>(this.invertedIndex.getOrDefault(Normalizer.normalize(canonicalName), Collections.emptySet()));
    }

    private Set<Integer> intersect(List<Set<Integer>> list){
        if(list.isEmpty()){
            return Collections.emptySet();
        }
        Set<Integer> positions = new HashSet<>(list.getFirst());
        for(int i = 1; i < list.size(); i++){
            positions.retainAll(list.get(i));
        }
        return positions;
    }
}
