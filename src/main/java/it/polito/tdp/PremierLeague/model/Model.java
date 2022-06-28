package it.polito.tdp.PremierLeague.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private Graph<Match,DefaultWeightedEdge> grafo;
	private PremierLeagueDAO dao;
	private Map<Integer, Match> idMap;
	private List<Match> best;
	private double bestPeso;
	public Model() {
		dao = new PremierLeagueDAO();
		idMap = new HashMap<>();
		for(Match m : dao.listAllMatches())
			idMap.put(m.getMatchID(), m);
	}
	public void creaGrafo(int minuti, int mese) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		Graphs.addAllVertices(this.grafo, this.dao.getVertici(mese));
		
		for(Collegate c : this.dao.getArchi(mese, minuti, idMap)) {
			Graphs.addEdgeWithVertices(this.grafo, c.getM1(), c.getM2(), c.getPeso());
		}
		
	}
	public List<Collegate> getConessioneMax(){
		int max = Integer.MIN_VALUE;
		List<Collegate> result = new ArrayList<>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>max) {
				max = (int)this.grafo.getEdgeWeight(e);
			}
		}
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)==max) {
				result.add(new Collegate (this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int)this.grafo.getEdgeWeight(e)));
			}
		}
		return result;
	}
	
	public List<Match> collegamento(Match m1, Match m2){
		
		best = new ArrayList<>();
		List<Match> parziale = new ArrayList<>();
		parziale.add(m1);
		ricorsiva(parziale, 0,  m2);
		
		return best;

	}
	
	private void ricorsiva(List<Match> parziale, int livello, Match m2) {
		
		Match ultimo = parziale.get(parziale.size()-1);
		
		if(livello == this.grafo.vertexSet().size()-1)
			return;
		
		if(peso(parziale)>bestPeso) {
			if(ultimo.equals(m2)) {
				best = new ArrayList<>(parziale);
				bestPeso = peso(parziale);
			}
		}
		for(Match m :  Graphs.neighborListOf(this.grafo, ultimo)) {
			
			DefaultWeightedEdge e = this.grafo.getEdge(ultimo, m);
			
			if(e!= null && !parziale.contains(m)) {
				
				if(!(m.getTeamHomeID() == ultimo.getTeamHomeID() && m.getTeamAwayID() == ultimo.getTeamAwayID())
					&& !(m.getTeamHomeID() == ultimo.getTeamAwayID() && m.getTeamAwayID() == ultimo.getTeamHomeID()) 
					&& !parziale.contains(m)){
					parziale.add(m);
					ricorsiva(parziale, livello+1, m2);
					parziale.remove(parziale.size()-1);
				}
			}
		}
		
	}
	

	
	public int peso(List<Match> parziale) {
		int peso = 0;
		for(int i = 0; i< parziale.size()-1; i++) {
			peso+= this.grafo.getEdgeWeight(this.grafo.getEdge(parziale.get(i), parziale.get(i+1)));
		}
		return peso;
	}
	public int getNVertici(){
		return this.grafo.vertexSet().size();
	}

	public int getNArchi(){
		return this.grafo.edgeSet().size();
	}
	public Set<Match> getVertici() {
		return this.grafo.vertexSet();
	}
}
