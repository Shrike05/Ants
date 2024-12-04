package org.evensen.ants;

import javafx.geometry.Pos;
import java.util.Random;

public class MyAntWorld implements AntWorld{

    int WORLD_WIDTH;
    int WORLD_HEIGHT;
    int n_food_sources;
    FoodSource[] food_sources;
    float[][] food_pheromone;
    float[][] foraging_pheromone;
    
    boolean[][] food_mask;

    public MyAntWorld(int WORLD_WIDTH, int WORLD_HEIGHT, int n_food_sources){
        this.WORLD_WIDTH = WORLD_WIDTH;
        this.WORLD_HEIGHT = WORLD_HEIGHT;
        this.n_food_sources =n_food_sources;

        food_sources = new FoodSource[n_food_sources];

        for(int i = 0; i < n_food_sources; i++){
            food_sources[i] = new FoodSource();
        }

        food_pheromone = new float[WORLD_WIDTH][WORLD_HEIGHT];
        foraging_pheromone = new float[WORLD_WIDTH][WORLD_HEIGHT];
        food_mask = new boolean[WORLD_WIDTH][WORLD_HEIGHT];

        update_food_mask();
    }

    void update_food_mask(){
        for(int x = 0; x < food_mask.length; x++){
            boolean[] row = food_mask[x];
            for(int y = 0; y < row.length; y++){
                Position pos = new Position(x, y);

                food_mask[x][y] = false;

                for(FoodSource foodSource : food_sources){
                    if(pos.isWithinRadius(foodSource.pos, foodSource.r)){
                        food_mask[x][y] = true;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public int getWidth() {
        return WORLD_WIDTH;
    }

    @Override
    public int getHeight() {
        return WORLD_HEIGHT;
    }

    @Override
    public boolean isObstacle(Position p) {
        return !p.isInBounds(WORLD_WIDTH, WORLD_HEIGHT);
    }

    @Override
    public void dropForagingPheromone(Position p, float amount) {
        int x_idx = (int)p.getX();
        int y_idx = (int)p.getY();
        foraging_pheromone[x_idx][y_idx] += amount;
    }
    
    @Override
    public void dropFoodPheromone(Position p, float amount) {
        int x_idx = (int)p.getX();
        int y_idx = (int)p.getY();
        food_pheromone[x_idx][y_idx] += amount;
    }
    
    @Override
    public void dropFood(Position p) {
        // Default behavior: do nothing
    }
    
    @Override
    public void pickUpFood(Position p) {
        // Default behavior: do nothing
        for(int i = 0; i < food_sources.length; i++){
            FoodSource food_src = food_sources[i];

            if(p.isWithinRadius(food_src.pos, food_src.r)){
                food_src.takeFood();

                if(!food_src.containsFood()){
                    food_sources[i] = new FoodSource();
                    update_food_mask();
                }
            }
        }
    }
    
    @Override
    public float getDeadAntCount(Position p) {
        // Default return value
        return 0.0f;
    }
    
    @Override
    public float getForagingStrength(Position p) {
        int x_idx = (int)p.getX();
        int y_idx = (int)p.getY();
        return foraging_pheromone[x_idx][y_idx];
    }
    
    @Override
    public float getFoodStrength(Position p) {
        int x_idx = (int)p.getX();
        int y_idx = (int)p.getY();
        return food_pheromone[x_idx][y_idx];
    }
    
    @Override
    public boolean containsFood(Position p) {
        return food_mask[(int)p.getX()][(int)p.getY()];
    }
    
    @Override
    public long getFoodCount() {
        // Default return value
        return 0L;
    }
    
    @Override
    public boolean isHome(Position p) {
        // Default return value
        return p.isWithinRadius(new Position(WORLD_WIDTH, WORLD_HEIGHT/2), 20);
    }
    
    @Override
    public void dispersePheromones() {
        // Default behavior: do nothing
    }
    
    @Override
    public void setObstacle(Position p, boolean add) {
        // Default behavior: do nothing
    }
    
    @Override
    public void hitObstacle(Position p, float strength) {
        // Default behavior: do nothing
    }    

    class FoodSource{
        Position pos;
        float r;
        int bites;

        public FoodSource(){
            Random rand = new Random();

            this.pos = new Position(rand.nextInt(WORLD_WIDTH), rand.nextInt(WORLD_HEIGHT));
            this.r = 20;
            bites = 100;
        }

        boolean containsFood(){
            return bites > 0;
        }

        void takeFood(){
            if(containsFood()){
                bites--;
            }
        }
    }
}
