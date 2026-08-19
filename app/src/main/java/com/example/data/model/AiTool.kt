package com.example.data.model

data class AiTool(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val iconType: String,
    val accentColorHex: Long,
    val placeholderPrompt: String,
    val systemInstruction: String,
    val samplePrompts: List<String>
)

object AiToolRegistry {
    val tools = listOf(
        AiTool(
            id = "ai_chat",
            title = "AI Chat",
            description = "Unconstrained conversational superintelligence with deep context awareness.",
            category = "Core",
            iconType = "chat",
            accentColorHex = 0xFF00F2FE,
            placeholderPrompt = "Ask anything, brainstorm, or explore concepts...",
            systemInstruction = "You are NEXORA AI. Provide intelligent, comprehensive, and crisp responses in markdown.",
            samplePrompts = listOf(
                "Explain quantum supremacy in simple terms",
                "How do modern LLMs handle long context windows?",
                "Suggest 5 high-yield daily cognitive habits"
            )
        ),
        AiTool(
            id = "image_generator",
            title = "Image Generator",
            description = "High-definition neural visual synthesis with cinematic styles and aspect ratio control.",
            category = "Vision",
            iconType = "image_gen",
            accentColorHex = 0xFFFF007F,
            placeholderPrompt = "Describe the visual you want to generate in vivid detail...",
            systemInstruction = "You are an expert prompt engineer and visual synthesizer for Imagen/Gemini vision engines.",
            samplePrompts = listOf(
                "Futuristic cybernetic skyline with holographic billboards at dusk, neon reflections, 8k render",
                "An astronaut floating near a glowing crystalline nebula, cinematic lighting",
                "Minimalist matte dark ceramic cyber-samurai helmet with glowing cyan optics"
            )
        ),
        AiTool(
            id = "image_analyzer",
            title = "Image Analyzer",
            description = "Multimodal vision engine to inspect photos, diagrams, UI wireframes, and OCR text.",
            category = "Vision",
            iconType = "image_scan",
            accentColorHex = 0xFF9D00FF,
            placeholderPrompt = "Upload or select an image to inspect details, OCR, or diagnose...",
            systemInstruction = "You are NEXORA Vision Analyzer. Inspect images thoroughly, identifying objects, spatial relationships, typography, aesthetics, code snippets, or anomalies.",
            samplePrompts = listOf(
                "Analyze the composition, colors, and lighting of this image",
                "Extract all text and transcribe diagrams into structured markdown",
                "Identify any technical issues or UI flaws in this screenshot"
            )
        ),
        AiTool(
            id = "coding_assistant",
            title = "Coding Assistant",
            description = "Software architect for debugging, refactoring, writing algorithms, and architecture design.",
            category = "Engineering",
            iconType = "code",
            accentColorHex = 0xFF00FFA3,
            placeholderPrompt = "Paste code snippet, stack trace, or architecture requirements...",
            systemInstruction = "You are NEXORA Code Assistant. Write production-ready, clean, well-commented code. Always include code blocks with language tags.",
            samplePrompts = listOf(
                "Write a thread-safe LRU cache in Kotlin with coroutine support",
                "Optimize this recursive graph traversal algorithm for O(V+E)",
                "Review this REST API endpoint design and suggest security hardening"
            )
        ),
        AiTool(
            id = "study_assistant",
            title = "Study Assistant",
            description = "Adaptive tutor creating flashcards, quizzes, and intuitive conceptual analogies.",
            category = "Knowledge",
            iconType = "school",
            accentColorHex = 0xFFFFB300,
            placeholderPrompt = "Enter a complex topic or chapter you need to master...",
            systemInstruction = "You are NEXORA Study Tutor. Use the Feynman technique: break down hard subjects into intuitive concepts, follow with 3 quiz questions and answers.",
            samplePrompts = listOf(
                "Teach me Bayesian probability using practical everyday analogies",
                "Create a 5-question active recall quiz on cellular respiration",
                "Summarize the key events and geopolitical impact of the Industrial Revolution"
            )
        ),
        AiTool(
            id = "writing_assistant",
            title = "Writing Assistant",
            description = "High-impact storytelling, technical prose, email drafting, and tone transformation.",
            category = "Creative",
            iconType = "edit_note",
            accentColorHex = 0xFF00D2FF,
            placeholderPrompt = "Outline the article, pitch deck, email, or story you want to draft...",
            systemInstruction = "You are NEXORA Writing Assistant. Craft persuasive, elegant, and crisp copy tailored precisely to the user's intended audience.",
            samplePrompts = listOf(
                "Draft an executive memo announcing a major AI infrastructure upgrade",
                "Write a compelling sci-fi story opening set in a solar colony in 2184",
                "Rewrite this paragraph to sound authoritative, concise, and professional"
            )
        ),
        AiTool(
            id = "summarizer",
            title = "Summarizer",
            description = "Instant TL;DR, bulleted executive briefings, key takeaway extraction, and action items.",
            category = "Productivity",
            iconType = "summarize",
            accentColorHex = 0xFFFA709A,
            placeholderPrompt = "Paste lengthy text, article, or meeting transcript to condense...",
            systemInstruction = "You are NEXORA Summarizer. Output a concise TL;DR (2-3 sentences), Key Highlights (bullet points), and Action Items.",
            samplePrompts = listOf(
                "Summarize the key trade-offs between Monoliths and Microservices",
                "Condense a product quarterly review into a 3-bullet executive briefing",
                "Extract action items and deadlines from a project meeting transcript"
            )
        ),
        AiTool(
            id = "translator",
            title = "Translator",
            description = "Nuanced multi-language translation preserving context, tone, and idioms.",
            category = "Productivity",
            iconType = "translate",
            accentColorHex = 0xFF4FACFE,
            placeholderPrompt = "Enter text and target language (e.g. 'Translate to Japanese: ...')...",
            systemInstruction = "You are NEXORA Polyglot Translator. Provide the exact translation, phonetic pronunciation if applicable, tone notes, and 2 contextual variants (formal vs casual).",
            samplePrompts = listOf(
                "Translate 'Let's schedule a strategic sync tomorrow' to German, Japanese, and Spanish",
                "How do you formally negotiate price discounts in Mandarin Chinese?",
                "Translate this technical developer documentation to French"
            )
        ),
        AiTool(
            id = "math_solver",
            title = "Math Solver",
            description = "Step-by-step calculus, linear algebra, statistics, and discrete mathematics engine.",
            category = "Knowledge",
            iconType = "calculate",
            accentColorHex = 0xFFFF5E3A,
            placeholderPrompt = "Enter an equation, differential equation, or calculus problem...",
            systemInstruction = "You are NEXORA Math Engine. State the given equation, identify the theorem/rule, show step-by-step algebra derivation, and provide the final boxed answer.",
            samplePrompts = listOf(
                "Solve the integral of x^2 * e^(3x) dx with integration by parts",
                "Find the eigenvalues and eigenvectors of a 2x2 matrix [[4, 2], [1, 3]]",
                "Calculate the limit as x approaches 0 of (sin(5x) / (3x))"
            )
        ),
        AiTool(
            id = "research_mode",
            title = "Web / Research Mode",
            description = "Deep investigative synthesis, literature reviews, and cross-domain factual analysis.",
            category = "Knowledge",
            iconType = "travel_explore",
            accentColorHex = 0xFF7F00FF,
            placeholderPrompt = "Enter your research hypothesis, comparative study, or market question...",
            systemInstruction = "You are NEXORA Research Intelligence. Provide an objective, multi-perspective breakdown citing methodologies, trade-offs, and empirical findings.",
            samplePrompts = listOf(
                "Comparative analysis: Solid-State Batteries vs Lithium-Ion for commercial EVs",
                "Deep dive into Quantum Error Correction techniques: Surface Codes vs Bosonic Codes",
                "Current state of Fusion Energy breakthroughs and net energy gain milestones"
            )
        ),
        AiTool(
            id = "document_analyzer",
            title = "Document Analyzer",
            description = "Deconstruct legal contracts, terms of service, technical specs, and financial reports.",
            category = "Productivity",
            iconType = "description",
            accentColorHex = 0xFF00E5FF,
            placeholderPrompt = "Paste agreement text or document clauses to audit risks and terms...",
            systemInstruction = "You are NEXORA Document Auditor. Analyze the text for: Key Provisions, Hidden Obligations, Legal/Financial Risks, and Renegotiation Recommendations.",
            samplePrompts = listOf(
                "Audit this standard SaaS Non-Disclosure Agreement for one-sided clauses",
                "Extract all SLA guarantees and penalty conditions from this cloud vendor contract",
                "Highlight ambiguous IP assignment clauses in this contractor agreement"
            )
        ),
        AiTool(
            id = "creative_assistant",
            title = "Creative Assistant",
            description = "Brainstorm disruptive product ideas, catchy brand names, world-building, and slogans.",
            category = "Creative",
            iconType = "psychology",
            accentColorHex = 0xFFFF2A6D,
            placeholderPrompt = "Describe your creative project, game lore, startup concept, or theme...",
            systemInstruction = "You are NEXORA Creative Matrix. Offer 5 wildly imaginative yet viable creative angles, distinctive nomenclature, and immersive worldbuilding concepts.",
            samplePrompts = listOf(
                "Generate 5 evocative brand names and taglines for an autonomous drone logistics startup",
                "Create a detailed cyberpunk city faction: background, tech signature, and philosophy",
                "Brainstorm 3 unique gameplay mechanics for a gravity-defying puzzle game"
            )
        )
    )

    fun getById(id: String): AiTool {
        return tools.find { it.id == id } ?: tools.first()
    }
}
