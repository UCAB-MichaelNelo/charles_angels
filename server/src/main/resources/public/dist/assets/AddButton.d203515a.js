import{g as S,e as _,k as b,f as A,_ as s,h as R,i as p,r as k,l as $,m as M,n as U,j as i,o as F,b as j,F as B,p as T,T as E,B as L,L as O}from"./index.6f94c60a.js";import{A as X}from"./houses.bdd5c657.js";function z(t){return String(t).match(/[\d.\-+]*\s*(.*)/)[1]||""}function N(t){return parseFloat(t)}function P(t){return S("MuiSkeleton",t)}_("MuiSkeleton",["root","text","rectangular","circular","pulse","wave","withChildren","fitContent","heightAuto"]);const K=["animation","className","component","height","style","variant","width"];let l=t=>t,g,m,f,v;const W=t=>{const{classes:e,variant:a,animation:n,hasChildren:o,width:h,height:r}=t;return U({root:["root",a,n,o&&"withChildren",o&&!h&&"fitContent",o&&!r&&"heightAuto"]},P,e)},I=b(g||(g=l`
  0% {
    opacity: 1;
  }

  50% {
    opacity: 0.4;
  }

  100% {
    opacity: 1;
  }
`)),V=b(m||(m=l`
  0% {
    transform: translateX(-100%);
  }

  50% {
    /* +0.5s of delay between each loop */
    transform: translateX(100%);
  }

  100% {
    transform: translateX(100%);
  }
`)),q=A("span",{name:"MuiSkeleton",slot:"Root",overridesResolver:(t,e)=>{const{ownerState:a}=t;return[e.root,e[a.variant],a.animation!==!1&&e[a.animation],a.hasChildren&&e.withChildren,a.hasChildren&&!a.width&&e.fitContent,a.hasChildren&&!a.height&&e.heightAuto]}})(({theme:t,ownerState:e})=>{const a=z(t.shape.borderRadius)||"px",n=N(t.shape.borderRadius);return s({display:"block",backgroundColor:t.vars?t.vars.palette.Skeleton.bg:R(t.palette.text.primary,t.palette.mode==="light"?.11:.13),height:"1.2em"},e.variant==="text"&&{marginTop:0,marginBottom:0,height:"auto",transformOrigin:"0 55%",transform:"scale(1, 0.60)",borderRadius:`${n}${a}/${Math.round(n/.6*10)/10}${a}`,"&:empty:before":{content:'"\\00a0"'}},e.variant==="circular"&&{borderRadius:"50%"},e.hasChildren&&{"& > *":{visibility:"hidden"}},e.hasChildren&&!e.width&&{maxWidth:"fit-content"},e.hasChildren&&!e.height&&{height:"auto"})},({ownerState:t})=>t.animation==="pulse"&&p(f||(f=l`
      animation: ${0} 1.5s ease-in-out 0.5s infinite;
    `),I),({ownerState:t,theme:e})=>t.animation==="wave"&&p(v||(v=l`
      position: relative;
      overflow: hidden;

      /* Fix bug in Safari https://bugs.webkit.org/show_bug.cgi?id=68196 */
      -webkit-mask-image: -webkit-radial-gradient(white, black);

      &::after {
        animation: ${0} 1.6s linear 0.5s infinite;
        background: linear-gradient(
          90deg,
          transparent,
          ${0},
          transparent
        );
        content: '';
        position: absolute;
        transform: translateX(-100%); /* Avoid flash during server-side hydration */
        bottom: 0;
        left: 0;
        right: 0;
        top: 0;
      }
    `),V,(e.vars||e).palette.action.hover)),D=k.exports.forwardRef(function(e,a){const n=$({props:e,name:"MuiSkeleton"}),{animation:o="pulse",className:h,component:r="span",height:c,style:C,variant:y="text",width:x}=n,d=M(n,K),u=s({},n,{animation:o,component:r,variant:y,hasChildren:Boolean(d.children)}),w=W(u);return i(q,s({as:r,ref:a,className:F(w.root,h),ownerState:u},d,{style:s({width:x,height:c},C)}))});var J=D,Q="/assets/not-found-image.ace30200.jpeg";function Y({popoverText:t,route:e}){const[a,n]=k.exports.useState(null);return j(B,{children:[i(T,{id:"logout-popover",sx:{pointerEvents:"none"},open:!!a,anchorEl:a,anchorOrigin:{vertical:"bottom",horizontal:"center"},transformOrigin:{vertical:"top",horizontal:"right"},onClose:()=>n(null),disableRestoreFocus:!0,children:i(E,{variant:"body1",sx:{p:1},children:t})}),i(L,{variant:"contained",component:O,to:e,onMouseEnter:o=>n(o.currentTarget),onMouseLeave:()=>n(null),children:i(X,{fontSize:"large"})})]})}export{Y as A,J as S,Q as n};
