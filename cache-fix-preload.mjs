export default function fixCachePreload() {
    return {
        name: 'cache-fix-preload',
        setup({ config }) {
            console.log('[preload] cache-fix loaded - fixing scattered blocks on resume');
            
            const originalMessages = config.messages;
            if (!originalMessages) return;
            
            const isRelocatableBlock = (text) => {
                if (!text || typeof text !== 'string') return false;
                const SR = "<system-reminder>\n";
                return text.startsWith(SR + "The following deferred tools are now available") ||
                       text.startsWith(SR + "The following MCP tools are now available") ||
                       text.startsWith(SR + "The following skills are now available");
            };
            
            const ORDER = ["deferred", "mcp", "skills", "hooks"];
            
            let firstUserIdx = -1;
            for (let i = 0; i < originalMessages.length; i++) {
                if (originalMessages[i].role === "user") {
                    firstUserIdx = i;
                    break;
                }
            }
            if (firstUserIdx === -1) return originalMessages;
            
            let hasScatteredBlocks = false;
            for (let i = firstUserIdx + 1; i < originalMessages.length && !hasScatteredBlocks; i++) {
                const msg = originalMessages[i];
                if (msg.role !== "user" || !Array.isArray(msg.content)) continue;
                for (const block of msg.content) {
                    if (isRelocatableBlock(block.text || "")) {
                        hasScatteredBlocks = true;
                        break;
                    }
                }
            }
            
            if (!hasScatteredBlocks) return originalMessages;
            
            console.log('[cache-fix] Found scattered blocks, rebuilding...');
            
            const newMessages = [...originalMessages];
            const collectedBlocks = [];
            
            for (let i = 0; i < newMessages.length; i++) {
                const msg = newMessages[i];
                if (msg.role !== "user" || !Array.isArray(msg.content)) continue;
                
                const newContent = [];
                for (const block of msg.content) {
                    if (isRelocatableBlock(block.text || "")) {
                        collectedBlocks.push(block);
                    } else {
                        newContent.push(block);
                    }
                }
                msg.content = newContent;
            }
            
            collectedBlocks.sort((a, b) => {
                const getType = (text) => {
                    if (text.includes("deferred tools")) return "deferred";
                    if (text.includes("MCP tools")) return "mcp";
                    if (text.includes("skills")) return "skills";
                    return "hooks";
                };
                return ORDER.indexOf(getType(a.text)) - ORDER.indexOf(getType(b.text));
            });
            
            if (collectedBlocks.length > 0 && newMessages[firstUserIdx] && Array.isArray(newMessages[firstUserIdx].content)) {
                newMessages[firstUserIdx].content.unshift(...collectedBlocks);
            }
            
            return newMessages;
        }
    };
}