package org.example.test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Interview {

    public ConcurrentHashMap<String, LinkedHashSet<Integer>> getIndexMap(Book book, List<String> keywords) throws ExecutionException, InterruptedException {
        //TODO:: change LinkedHashSet to be thread safe DS
        ConcurrentHashMap<String, LinkedHashSet<Integer>> indexMap = new ConcurrentHashMap<>();
        initIndexMap(keywords, indexMap);
        List<Page> pages = book.pages;
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        CompletionService<ExecutorService> completionService = new ExecutorCompletionService<>(executorService);

        for (Page page : pages) {
            executorService.submit(() -> updateIndexMapFromPage(page, indexMap));
        }

        try {
            completionService.take().get();
        } catch (InterruptedException e) {

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return indexMap;
    }

    private static void updateIndexMapFromPage(Page page, Map<String, LinkedHashSet<Integer>> indexMap) {
        List<String> words = page.getWords().stream().map(String::toLowerCase).collect(Collectors.toList());

        for(String word : words){
            if(indexMap.containsKey(word)){
                Set<Integer> pageNumers = indexMap.get(word);
                pageNumers.add(page.number);
            }
        }
    }

    private static void initIndexMap(List<String> keywords, Map<String, LinkedHashSet<Integer>> indexMap) {
        for(String keyword: keywords){
            indexMap.put(keyword.toLowerCase(), new LinkedHashSet<>());
        }
    }


    class Book {
        private String Title;
        private List<Page> pages;

        public String getTitle() {
            return Title;
        }

        public void setTitle(String title) {
            Title = title;
        }
    }

    class Page {
        private Integer number;
        private List<String> words;

        public List<String> getWords() {
            return words;
        }

        public void setWords(List<String> words) {
            this.words = words;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }
    }
}
