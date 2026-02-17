package dev.rage4j.demo.resource;

import dev.rage4j.demo.model.EvaluationRequest;
import dev.rage4j.demo.model.EvaluationResult;
import dev.rage4j.demo.model.MetricInfo;
import dev.rage4j.demo.service.EvaluationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvaluationResource {

    @Inject
    EvaluationService evaluationService;

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of(
            "status", "healthy",
            "configured", evaluationService.isConfigured()
        )).build();
    }

    @GET
    @Path("/metrics")
    public List<MetricInfo> getMetrics() {
        return evaluationService.getAvailableMetrics();
    }

    @POST
    @Path("/evaluate")
    public List<EvaluationResult> evaluate(EvaluationRequest request) {
        return evaluationService.evaluate(request);
    }

    @GET
    @Path("/examples")
    public List<Map<String, Object>> getExamples() {
        return List.of(
            Map.of(
                "name", "Capital City Question",
                "question", "What is the capital of France?",
                "answer", "The capital of France is Paris. It is located in the north-central part of the country.",
                "groundTruth", "Paris is the capital of France.",
                "contexts", List.of(
                    "Paris is the capital and largest city of France. It is situated on the Seine River.",
                    "France is a country in Western Europe with Paris as its capital city."
                )
            ),
            Map.of(
                "name", "Scientific Fact",
                "question", "What is photosynthesis?",
                "answer", "Photosynthesis is the process by which plants convert sunlight into energy, using carbon dioxide and water to produce glucose and oxygen.",
                "groundTruth", "Photosynthesis is a process used by plants to convert light energy into chemical energy stored in glucose.",
                "contexts", List.of(
                    "Photosynthesis is a biological process where plants, algae, and some bacteria convert light energy into chemical energy.",
                    "During photosynthesis, plants absorb carbon dioxide and water, and using sunlight, produce glucose and release oxygen."
                )
            ),
            Map.of(
                "name", "Historical Event",
                "question", "When did World War II end?",
                "answer", "World War II ended in 1945, with Germany surrendering in May and Japan surrendering in September after the atomic bombings.",
                "groundTruth", "World War II ended in 1945.",
                "contexts", List.of(
                    "World War II was a global conflict that lasted from 1939 to 1945.",
                    "Germany surrendered unconditionally on May 8, 1945 (V-E Day). Japan surrendered on September 2, 1945 (V-J Day) after atomic bombs were dropped on Hiroshima and Nagasaki."
                )
            ),
            Map.of(
                "name", "Hallucination Example",
                "question", "What are the main features of Python?",
                "answer", "Python is a programming language created by Guido van Rossum in 1991. It features dynamic typing, automatic memory management, and was originally designed for the Mars Rover project at NASA.",
                "groundTruth", "Python is a high-level programming language known for its readability, dynamic typing, and extensive standard library.",
                "contexts", List.of(
                    "Python was created by Guido van Rossum and first released in 1991.",
                    "Python emphasizes code readability and has a design philosophy that uses significant indentation."
                )
            )
        );
    }
}
