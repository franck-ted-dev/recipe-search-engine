package com.github.franckteddev.search.search;

import com.github.franckteddev.search.model.Ingredient;
import com.github.franckteddev.search.model.Recipe;
import com.github.franckteddev.search.store.Storage;
import com.github.franckteddev.search.utility.Normalizer;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InvertedIndexSearchEngine implements SearchEngine{
    private final Map<String, Set<Integer>> invertedIndex;
    private final List<Recipe> recipes;
    private static final Logger logger = Logger.getLogger(InvertedIndexSearchEngine.class.getName());
    private final AllStrategy allStrategy;
    private final AnyStrategy anyStrategy;
    private final NoneStrategy noneStrategy;

    public InvertedIndexSearchEngine(Storage<Recipe> storage){
        this.allStrategy = new AllStrategy();
        this.anyStrategy = new AnyStrategy();
        this.noneStrategy = new NoneStrategy();
        this.invertedIndex = new HashMap<>();
        this.recipes = storage.getAll();
        int pos = 0;

        for (Recipe recipe : recipes){
            List<Ingredient> ingredients = recipe.ingredients();
            for (Ingredient ingredient : ingredients){
                String ingredientName = Normalizer.normalize(ingredient.name());
                if (!ingredientName.isEmpty()) {
                    invertedIndex.computeIfAbsent(ingredientName, k -> new HashSet<>()).add(pos);
                }else {
                    logger.warning("Ingredient name for recipe " + recipe.name() + " is empty after normalization");
                }
            }
            pos++;
        }
    }

    public boolean isReady(){
        return !recipes.isEmpty() && !invertedIndex.isEmpty();
    }

    @Override
    public List<Recipe> search(SearchRequestParam query) {
        List<String> all = query.all();
        List<String> any = query.any();
        List<String> none = query.none();

        List<Set<Integer>> list = new ArrayList<>();

        if(!all.isEmpty()){
            List<Set<Integer>> positionsList1 = new ArrayList<>();
            for(String ingredientName: all){
                positionsList1.add(this.search(ingredientName));
            }
            Set<Integer> positionsAfterAllStrategy = this.allStrategy.executeStrategy(positionsList1, this.recipes.size());
            list.add(positionsAfterAllStrategy);
        }

        if(!any.isEmpty()){
            List<Set<Integer>> positionsList2 = new ArrayList<>();
            for(String ingredientName: any){
                positionsList2.add(this.search(ingredientName));
            }
            Set<Integer> positionsAfterAnyStrategy = this.anyStrategy.executeStrategy(positionsList2, this.recipes.size());
            list.add(positionsAfterAnyStrategy);
        }

        if(!none.isEmpty()){
            List<Set<Integer>> positionsList3 = new ArrayList<>();
            for(String ingredientName: none){
                positionsList3.add(this.search(ingredientName));
            }
            Set<Integer> positionsAfterNoneStrategy = this.noneStrategy.executeStrategy(positionsList3, this.recipes.size());
            list.add(positionsAfterNoneStrategy);
        }

        return this.intersect(list).stream().map(recipes::get).sorted(Comparator.comparing(Recipe::name, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
    }

    private Set<Integer> search(String ingredientName){
        return new HashSet<>(this.invertedIndex.getOrDefault(Normalizer.normalize(ingredientName), Collections.emptySet()));
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
