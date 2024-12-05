package org.evensen.ants;

import java.util.Random;

public class MyAntWorld implements AntWorld{

    int WORLD_WIDTH;
    int WORLD_HEIGHT;
    int n_food_sources;
    FoodSource[] food_sources;
    public float[][] food_pheromone;
    public float[][] foraging_pheromone;
    
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

        foraging_pheromone[x_idx][y_idx] = Math.min(1f, foraging_pheromone[x_idx][y_idx]);
    }
    
    @Override
    public void dropFoodPheromone(Position p, float amount) {
        int x_idx = (int)p.getX();
        int y_idx = (int)p.getY();
        food_pheromone[x_idx][y_idx] += amount;

        food_pheromone[x_idx][y_idx] = Math.min(1f, food_pheromone[x_idx][y_idx]);
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
    public void dispersePheromones(DispersalPolicy policy) {
        float[][][] phermone_map = new float[2][WORLD_WIDTH][WORLD_HEIGHT];
        
        for(int x = 0; x < WORLD_WIDTH; x++){
            for(int y = 0; y < WORLD_HEIGHT; y++){
                float[] res = policy.getDispersedValue(this, new Position(x, y));
                phermone_map[0][x][y] = res[0];
                phermone_map[1][x][y] = res[1];
            }
        }

        food_pheromone = phermone_map[0];
        foraging_pheromone = phermone_map[1];
    }
    
    @Override
    public void selfContainedDisperse() {
        float[][][] pheromone_map = dispersePheromone_();
        food_pheromone = pheromone_map[0];
        foraging_pheromone = pheromone_map[1];
    }

    float[][][] dispersePheromone_(){
        float f = 0.95f;
        float k = 0.5f;
        float[][][] phermone_map = new float[2][WORLD_WIDTH][WORLD_HEIGHT];
        
        for(int x = 0; x < WORLD_WIDTH; x++){
            for(int y = 0; y < WORLD_HEIGHT; y++){
                float[] nlp = {0,0};
                nlp = add_phermones_at(x-1, y+1, nlp);
                nlp = add_phermones_at(x, y+1, nlp);
                nlp = add_phermones_at(x+1, y+1, nlp);

                nlp = add_phermones_at(x+1, y, nlp);
                nlp = add_phermones_at(x-1, y, nlp);

                nlp = add_phermones_at(x-1, y-1, nlp);
                nlp = add_phermones_at(x, y-1, nlp);
                nlp = add_phermones_at(x+1, y-1, nlp);

                
                nlp[0] = (1-k)*nlp[0]/8 + (k*food_pheromone[x][y]);
                nlp[1] = (1-k)*nlp[1]/8 + (k*foraging_pheromone[x][y]);

                phermone_map[0][x][y] = nlp[0] * f;
                phermone_map[1][x][y] = nlp[1] * f;
            }
        }

        return phermone_map;
    }

    float[] add_phermones_at(int x, int y, float[] nlp){
        int x_idx = Math.max(Math.min(x, WORLD_WIDTH-1), 0);
        int y_idx = Math.max(Math.min(y, WORLD_HEIGHT-1), 0);

        Position pos = new Position(x_idx, y_idx);

        float[] answer = {nlp[0] + getFoodStrength(pos), nlp[1] + getForagingStrength(pos)};
        return answer;
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
