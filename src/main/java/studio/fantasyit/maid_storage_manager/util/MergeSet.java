package studio.fantasyit.maid_storage_manager.util;

public class MergeSet {
    int[] parent;
    public MergeSet(int size){
        parent = new int[size];
        for(int i=0;i<size;i++){
            parent[i] = i;
        }
    }
    public int find(int x){
        if(parent[x] == x){
            return x;
        }
        return parent[x]=find(parent[x]);
    }
    public void merge(int x, int x2){
        parent[find(x)] = find(x2);
    }
}
