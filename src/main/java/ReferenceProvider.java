import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReferenceProvider {

    public ReferenceProvider() {
    }

    public List<Location> getReferences(ReferenceParams params) {
        Range cursor = new Range(params.getPosition(), params.getPosition());
        SouffleContext context = ProjectContext.getInstance().getContext(params.getTextDocument().getUri(), cursor);
        List<Location> references = new ArrayList<Location>();
        if (context != null) {
            SouffleSymbol currentSymbol = context.getSymbol(cursor);
            for (Map.Entry<String, SouffleContext> documentContext : ProjectContext.getInstance().getDocuments().entrySet()) {
                Optional.ofNullable(documentContext.getValue()
                                .getSymbols(currentSymbol.getName()))
                        .ifPresent(souffleSymbols -> souffleSymbols.forEach(symbol -> references.add(new Location(documentContext.getKey(), symbol.getRange()))));
                for (SouffleContext ruleContext : documentContext.getValue().getSubContext().values()) {
                    if (ruleContext.getKind() != SouffleContextType.COMPONENT) {
                        Optional.ofNullable(ruleContext
                                        .getSymbols(currentSymbol.getName()))
                                .ifPresent(souffleSymbols ->
                                        souffleSymbols.forEach(symbol ->
                                                references.add(new Location(documentContext.getKey(), symbol.getRange()))));
                    }
                }
            }
        }
        return references;
    }
}