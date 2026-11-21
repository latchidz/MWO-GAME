(() => {
  if (location.hostname !== "web.nfsmwo.com") return;
  if (window.__mwoSinglePlayerAutoStartInstalled) return;
  window.__mwoSinglePlayerAutoStartInstalled = true;

  let attempts = 0;
  let finished = false;
  const normalize = value => (value || "").replace(/\s+/g, " ").trim().toLowerCase();

  function clickSinglePlayer() {
    if (finished) return true;
    attempts += 1;
    const nodes = document.querySelectorAll("button, a, [role='button'], [role='menuitem'], input[type='button'], input[type='submit'], div, span");
    for (const node of nodes) {
      if (!node || !node.isConnected) continue;
      const text = normalize(node.innerText || node.textContent || node.value);
      if (text !== "single player") continue;
      const rect = node.getBoundingClientRect();
      const style = getComputedStyle(node);
      if (rect.width < 2 || rect.height < 2 || style.visibility === "hidden" || style.display === "none") continue;
      // Select only the smallest exact-text element so parent containers do not
      // swallow the click or trigger an unrelated control.
      const smallerExactChild = [...node.querySelectorAll("*")].some(child => normalize(child.innerText || child.textContent || child.value) === "single player");
      if (smallerExactChild) continue;
      finished = true;
      window.__mwoSinglePlayerAutoStarted = true;
      node.click();
      window.dispatchEvent(new CustomEvent("mwo-single-player-started"));
      return true;
    }
    return false;
  }

  const timer = setInterval(() => {
    if (clickSinglePlayer() || attempts >= 120) clearInterval(timer);
  }, 500);
  const observer = new MutationObserver(() => {
    if (clickSinglePlayer()) {
      clearInterval(timer);
      observer.disconnect();
    }
  });
  if (document.documentElement) observer.observe(document.documentElement, { childList: true, subtree: true, characterData: true });
})();
