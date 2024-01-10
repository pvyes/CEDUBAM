package domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.bayesserver.Variable;

public class NamedEvidence {
	
	@SuppressWarnings("unused")
	private Diagnoser diagnoser;
	private String name;
	private Set<EvidenceMap> evidences;
	
	public NamedEvidence(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.evidences = new HashSet<EvidenceMap>();
	}
	
	public NamedEvidence(Diagnoser diagnoser, String name) {
		this.diagnoser = diagnoser;
		this.name = name;
		this.evidences = new HashSet<EvidenceMap>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isActivated() {
		boolean activated = true;
		for (EvidenceMap em: evidences) {
			activated = activated && em.isActivated();
		}
		return activated;
	}
	
	public NamedEvidence addEvidence(EvidenceMap evidence) {
		EvidenceMap ev = getEvidenceByVariable(evidence.getVariable());
		if (ev == null) {
			evidences.add(evidence);
		}
		return this;
	}
	
	private EvidenceMap getEvidenceByVariable(Variable var) {
		for (EvidenceMap em: evidences) {
			if (em.getVariable() == var) {
				return em;
			}
		}
		return null;
	}

	public NamedEvidence addEvidence(Collection<EvidenceMap> evidences) {
		this.evidences.addAll(evidences);
		return this;
	}
	
	public Set<EvidenceMap> getEvidences() {
		return evidences;
	}
	
	public Collection<Variable> getVariables() {
		Collection<Variable> result = new HashSet<Variable>();
		for (EvidenceMap em: evidences) {
			result.add(em.getVariable());
		}
		return result;
	}

	@Override
	public String toString() {
		String s = "";
		s += "Named Evidence: " + name + "\n";
		for (EvidenceMap em: evidences) {
			s += em.toString();
			s += "\n";
		}
		return s.substring(0, s.length() - 1);
	}
}
