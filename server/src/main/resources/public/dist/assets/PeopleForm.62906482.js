import{g as se,e as de,f as K,V as Ce,_ as R,r as f,m as ce,H as Se,a1 as fe,n as ue,b as z,j as a,o as he,U as X,l as ge,ab as ye,T as pe,h as me,ac as we,ad as ke,ae as $e,w as xe,af as ve,ag as Ee,F as ze,G as b,D as W,d as N,c as Le,z as Pe,A as Be,ah as Te,a5 as re,a6 as le,a7 as ie,R as Re,B as be}from"./index.6f94c60a.js";import{i as Ie,g as Fe,c as qe,e as Oe,f as De,u as Ve}from"./children.1f7fa8c5.js";import{A as Ae,I as Me,C as We,D as Y,a as je,L as _e}from"./LoadingButton.63d18e7c.js";import{e as Ge,B as He,A as Ne}from"./houses.bdd5c657.js";import{M as ee}from"./MenuItem.15897e82.js";import"./dividerClasses.ded1b393.js";function Ue(e){return se("PrivateSwitchBase",e)}de("PrivateSwitchBase",["root","checked","disabled","input","edgeStart","edgeEnd"]);const Xe=["autoFocus","checked","checkedIcon","className","defaultChecked","disabled","disableFocusRipple","edge","icon","id","inputProps","inputRef","name","onBlur","onChange","onFocus","readOnly","required","tabIndex","type","value"],Je=e=>{const{classes:l,checked:i,disabled:c,edge:t}=e,v={root:["root",i&&"checked",c&&"disabled",t&&`edge${X(t)}`],input:["input"]};return ue(v,Ue,l)},Ke=K(Ce)(({ownerState:e})=>R({padding:9,borderRadius:"50%"},e.edge==="start"&&{marginLeft:e.size==="small"?-3:-12},e.edge==="end"&&{marginRight:e.size==="small"?-3:-12})),Qe=K("input")({cursor:"inherit",position:"absolute",opacity:0,width:"100%",height:"100%",top:0,left:0,margin:0,padding:0,zIndex:1}),Ye=f.exports.forwardRef(function(l,i){const{autoFocus:c,checked:t,checkedIcon:v,className:T,defaultChecked:r,disabled:h,disableFocusRipple:j=!1,edge:y=!1,icon:C,id:O,inputProps:D,inputRef:I,name:_,onBlur:M,onChange:d,onFocus:u,readOnly:o,required:$,tabIndex:E,type:x,value:w}=l,V=ce(l,Xe),[G,J]=Se({controlled:t,default:Boolean(r),name:"SwitchBase",state:"checked"}),F=fe(),U=P=>{u&&u(P),F&&F.onFocus&&F.onFocus(P)},Q=P=>{M&&M(P),F&&F.onBlur&&F.onBlur(P)},L=P=>{if(P.nativeEvent.defaultPrevented)return;const Z=P.target.checked;J(Z),d&&d(P,Z)};let k=h;F&&typeof k=="undefined"&&(k=F.disabled);const A=x==="checkbox"||x==="radio",q=R({},l,{checked:G,disabled:k,disableFocusRipple:j,edge:y}),H=Je(q);return z(Ke,R({component:"span",className:he(H.root,T),centerRipple:!0,focusRipple:!j,disabled:k,tabIndex:null,role:void 0,onFocus:U,onBlur:Q,ownerState:q,ref:i},V,{children:[a(Qe,R({autoFocus:c,checked:t,defaultChecked:r,className:H.input,disabled:k,id:A&&O,name:_,onChange:L,readOnly:o,ref:I,required:$,ownerState:q,tabIndex:E,type:x},x==="checkbox"&&w===void 0?{}:{value:w},D)),G?v:C]}))});var Ze=Ye;function et(e){return se("MuiFormControlLabel",e)}const tt=de("MuiFormControlLabel",["root","labelPlacementStart","labelPlacementTop","labelPlacementBottom","disabled","label","error"]);var te=tt;const at=["checked","className","componentsProps","control","disabled","disableTypography","inputRef","label","labelPlacement","name","onChange","value"],rt=e=>{const{classes:l,disabled:i,labelPlacement:c,error:t}=e,v={root:["root",i&&"disabled",`labelPlacement${X(c)}`,t&&"error"],label:["label",i&&"disabled"]};return ue(v,et,l)},lt=K("label",{name:"MuiFormControlLabel",slot:"Root",overridesResolver:(e,l)=>{const{ownerState:i}=e;return[{[`& .${te.label}`]:l.label},l.root,l[`labelPlacement${X(i.labelPlacement)}`]]}})(({theme:e,ownerState:l})=>R({display:"inline-flex",alignItems:"center",cursor:"pointer",verticalAlign:"middle",WebkitTapHighlightColor:"transparent",marginLeft:-11,marginRight:16,[`&.${te.disabled}`]:{cursor:"default"}},l.labelPlacement==="start"&&{flexDirection:"row-reverse",marginLeft:16,marginRight:-11},l.labelPlacement==="top"&&{flexDirection:"column-reverse",marginLeft:16},l.labelPlacement==="bottom"&&{flexDirection:"column",marginLeft:16},{[`& .${te.label}`]:{[`&.${te.disabled}`]:{color:(e.vars||e).palette.text.disabled}}})),it=f.exports.forwardRef(function(l,i){const c=ge({props:l,name:"MuiFormControlLabel"}),{className:t,componentsProps:v={},control:T,disabled:r,disableTypography:h,label:j,labelPlacement:y="end"}=c,C=ce(c,at),O=fe();let D=r;typeof D=="undefined"&&typeof T.props.disabled!="undefined"&&(D=T.props.disabled),typeof D=="undefined"&&O&&(D=O.disabled);const I={disabled:D};["checked","name","onChange","value","inputRef"].forEach(o=>{typeof T.props[o]=="undefined"&&typeof c[o]!="undefined"&&(I[o]=c[o])});const _=ye({props:c,muiFormControl:O,states:["error"]}),M=R({},c,{disabled:D,labelPlacement:y,error:_.error}),d=rt(M);let u=j;return u!=null&&u.type!==pe&&!h&&(u=a(pe,R({component:"span",className:d.label},v.typography,{children:u}))),z(lt,R({className:he(d.root,t),ownerState:M,ref:i},C,{children:[f.exports.cloneElement(T,I),u]}))});var ne=it;function nt(e){return se("MuiSwitch",e)}const ot=de("MuiSwitch",["root","edgeStart","edgeEnd","switchBase","colorPrimary","colorSecondary","sizeSmall","sizeMedium","checked","disabled","input","thumb","track"]);var B=ot;const st=["className","color","edge","size","sx"],dt=e=>{const{classes:l,edge:i,size:c,color:t,checked:v,disabled:T}=e,r={root:["root",i&&`edge${X(i)}`,`size${X(c)}`],switchBase:["switchBase",`color${X(t)}`,v&&"checked",T&&"disabled"],thumb:["thumb"],track:["track"],input:["input"]},h=ue(r,nt,l);return R({},l,h)},ct=K("span",{name:"MuiSwitch",slot:"Root",overridesResolver:(e,l)=>{const{ownerState:i}=e;return[l.root,i.edge&&l[`edge${X(i.edge)}`],l[`size${X(i.size)}`]]}})(({ownerState:e})=>R({display:"inline-flex",width:34+12*2,height:14+12*2,overflow:"hidden",padding:12,boxSizing:"border-box",position:"relative",flexShrink:0,zIndex:0,verticalAlign:"middle","@media print":{colorAdjust:"exact"}},e.edge==="start"&&{marginLeft:-8},e.edge==="end"&&{marginRight:-8},e.size==="small"&&{width:40,height:24,padding:7,[`& .${B.thumb}`]:{width:16,height:16},[`& .${B.switchBase}`]:{padding:4,[`&.${B.checked}`]:{transform:"translateX(16px)"}}})),ut=K(Ze,{name:"MuiSwitch",slot:"SwitchBase",overridesResolver:(e,l)=>{const{ownerState:i}=e;return[l.switchBase,{[`& .${B.input}`]:l.input},i.color!=="default"&&l[`color${X(i.color)}`]]}})(({theme:e})=>({position:"absolute",top:0,left:0,zIndex:1,color:e.vars?e.vars.palette.Switch.defaultColor:`${e.palette.mode==="light"?e.palette.common.white:e.palette.grey[300]}`,transition:e.transitions.create(["left","transform"],{duration:e.transitions.duration.shortest}),[`&.${B.checked}`]:{transform:"translateX(20px)"},[`&.${B.disabled}`]:{color:e.vars?e.vars.palette.Switch.defaultDisabledColor:`${e.palette.mode==="light"?e.palette.grey[100]:e.palette.grey[600]}`},[`&.${B.checked} + .${B.track}`]:{opacity:.5},[`&.${B.disabled} + .${B.track}`]:{opacity:e.vars?e.vars.opacity.switchTrackDisabled:`${e.palette.mode==="light"?.12:.2}`},[`& .${B.input}`]:{left:"-100%",width:"300%"}}),({theme:e,ownerState:l})=>R({"&:hover":{backgroundColor:e.vars?`rgba(${e.vars.palette.action.activeChannel} / ${e.vars.palette.action.hoverOpacity})`:me(e.palette.action.active,e.palette.action.hoverOpacity),"@media (hover: none)":{backgroundColor:"transparent"}}},l.color!=="default"&&{[`&.${B.checked}`]:{color:(e.vars||e).palette[l.color].main,"&:hover":{backgroundColor:e.vars?`rgba(${e.vars.palette[l.color].mainChannel} / ${e.vars.palette.action.hoverOpacity})`:me(e.palette[l.color].main,e.palette.action.hoverOpacity),"@media (hover: none)":{backgroundColor:"transparent"}},[`&.${B.disabled}`]:{color:e.vars?e.vars.palette.Switch[`${l.color}DisabledColor`]:`${e.palette.mode==="light"?we(e.palette[l.color].main,.62):ke(e.palette[l.color].main,.55)}`}},[`&.${B.checked} + .${B.track}`]:{backgroundColor:(e.vars||e).palette[l.color].main}})),ht=K("span",{name:"MuiSwitch",slot:"Track",overridesResolver:(e,l)=>l.track})(({theme:e})=>({height:"100%",width:"100%",borderRadius:14/2,zIndex:-1,transition:e.transitions.create(["opacity","background-color"],{duration:e.transitions.duration.shortest}),backgroundColor:e.vars?e.vars.palette.common.onBackground:`${e.palette.mode==="light"?e.palette.common.black:e.palette.common.white}`,opacity:e.vars?e.vars.opacity.switchTrack:`${e.palette.mode==="light"?.38:.3}`})),pt=K("span",{name:"MuiSwitch",slot:"Thumb",overridesResolver:(e,l)=>l.thumb})(({theme:e})=>({boxShadow:(e.vars||e).shadows[1],backgroundColor:"currentColor",width:20,height:20,borderRadius:"50%"})),mt=f.exports.forwardRef(function(l,i){const c=ge({props:l,name:"MuiSwitch"}),{className:t,color:v="primary",edge:T=!1,size:r="medium",sx:h}=c,j=ce(c,st),y=R({},c,{color:v,edge:T,size:r}),C=dt(y),O=a(pt,{className:C.thumb,ownerState:y});return z(ct,{className:he(C.root,t),sx:h,ownerState:y,children:[a(ut,R({type:"checkbox",icon:O,checkedIcon:O,ref:i,ownerState:y},j,{classes:R({},C,{root:C.switchBase})})),a(ht,{className:C.track,ownerState:y})]})});var oe=mt;function ae({control:e,validateCiUniqueness:l=!1,personalInformation:i,noAuto:c=!1,fieldName:t,isForUpdate:v,firstColumnWidth:T=12}){const{errors:r}=$e({control:e}),h=xe(ve),{watch:j,setValue:y,resetField:C,setError:O,clearErrors:D}=Ee(),I=j(`${t}.ci`),[_,M]=f.exports.useState(!1);return l&&f.exports.useEffect(()=>{var d;h&&h.information.ci==I||((d=r==null?void 0:r[t])==null?void 0:d.ci)||!I||(M(!0),Ie(I).then(u=>{M(!1),u?D(`${t}.ci`):O(`${t}.ci`,{type:"unicity"})}))},[I]),c||f.exports.useEffect(()=>{const d=i==null?void 0:i[I];d?(y(`${t}.name`,d.name),y(`${t}.lastname`,d.lastname),y(`${t}.birthdate`,d.birthdate)):(C(`${t}.name`),C(`${t}.lastname`),C(`${t}.birthdate`))},[I]),z(ze,{children:[a(b,{item:!0,xs:T,children:a(W,{control:e,name:`${t}.ci`,defaultValue:"",rules:{required:!0,maxLength:9},render:({field:{onChange:d,...u}})=>{var o,$,E,x,w,V,G,J,F,U,Q;return c?a(N,{fullWidth:!0,error:!!((E=r==null?void 0:r[t])!=null&&E.ci),variant:"outlined",label:"C\xE9dula de identidad",type:"number",helperText:((w=(x=r==null?void 0:r[t])==null?void 0:x.ci)==null?void 0:w.type)=="required"?"La c\xE9dula no puede estar vac\xEDa":((G=(V=r==null?void 0:r[t])==null?void 0:V.ci)==null?void 0:G.type)=="maxLength"?"La c\xE9dula no puede tener m\xE1s de 9 d\xEDgitos":((F=(J=r==null?void 0:r[t])==null?void 0:J.ci)==null?void 0:F.type)=="unicity"?"Esta c\xE9dula ya est\xE1 en uso por otra persona":"",onChange:d,...u,value:(Q=(U=u.value)==null?void 0:U.toString())!=null?Q:""}):a(Ae,{freeSolo:!v,disableClearable:!0,fullWidth:!0,options:i?Object.keys(i):[],renderOption:(L,k)=>{var q,H;const A=Number(k);return z("span",{...L,children:[(q=i==null?void 0:i[A])==null?void 0:q.name," ",(H=i==null?void 0:i[A])==null?void 0:H.lastname," (",k,")"]})},loading:!i,renderInput:L=>{var k,A,q,H,P;return a(N,{...L,error:!!((k=r==null?void 0:r[t])!=null&&k.ci),variant:"outlined",label:"C\xE9dula de identidad",type:"number",helperText:((q=(A=r==null?void 0:r[t])==null?void 0:A.ci)==null?void 0:q.type)=="required"?"La c\xE9dula no puede estar vac\xEDa":((P=(H=r==null?void 0:r[t])==null?void 0:H.ci)==null?void 0:P.type)=="maxLength"?"La c\xE9dula no puede tener m\xE1s de 9 d\xEDgitos":"",InputProps:{...L.InputProps,endAdornment:a(Me,{position:"end",children:(!i||_)&&a(We,{size:"medium"})})}})},...u,value:($=(o=u.value)==null?void 0:o.toString())!=null?$:"",onInputChange:v?void 0:(L,k)=>d(k),onChange:v?(L,k)=>d(k):void 0})}})}),a(b,{item:!0,xs:4,children:a(W,{control:e,defaultValue:"",name:`${t}.name`,rules:{required:!0,maxLength:50},render:({field:d})=>{var u,o,$,E,x,w,V;return a(N,{fullWidth:!0,error:!!((u=r==null?void 0:r[t])!=null&&u.name),variant:"outlined",label:"Nombre",helperText:(($=(o=r==null?void 0:r[t])==null?void 0:o.name)==null?void 0:$.type)=="required"?"El nombre no puede estar vac\xEDo":((x=(E=r==null?void 0:r.information)==null?void 0:E.name)==null?void 0:x.type)=="maxLength"?"El nombre no puede tener m\xE1s de 50 caracteres":"",...d,value:(V=(w=d.value)==null?void 0:w.toString())!=null?V:"",InputProps:{readOnly:v&&!c}})}})}),a(b,{item:!0,xs:4,children:a(W,{control:e,name:`${t}.lastname`,rules:{required:!0,maxLength:75},defaultValue:"",render:({field:d})=>{var u,o,$,E,x,w,V;return a(N,{fullWidth:!0,error:!!((u=r==null?void 0:r[t])!=null&&u.lastname),variant:"outlined",label:"Apellido",helperText:(($=(o=r==null?void 0:r[t])==null?void 0:o.lastname)==null?void 0:$.type)=="required"?"El apellido no puede estar vac\xEDo":((x=(E=r==null?void 0:r.information)==null?void 0:E.lastname)==null?void 0:x.type)=="maxLength"?"El apellido no puede tener m\xE1s de 75 caracteres":"",...d,value:(V=(w=d.value)==null?void 0:w.toString())!=null?V:"",InputProps:{readOnly:v&&!c}})}})}),a(b,{item:!0,xs:4,children:a(W,{control:e,name:`${t}.birthdate`,rules:{required:!0},defaultValue:"1999-12-01",render:({field:d})=>{var u,o,$,E,x;return a(N,{fullWidth:!0,error:!!((u=r==null?void 0:r[t])!=null&&u.birthdate),variant:"outlined",label:"Fecha de nacimiento",type:"date",helperText:(($=(o=r==null?void 0:r[t])==null?void 0:o.birthdate)==null?void 0:$.type)=="required"?"El apellido no puede estar vac\xEDo":"",...d,value:(x=(E=d.value)==null?void 0:E.toString())!=null?x:"1999-12-01",InputProps:{readOnly:v&&!c}})}})})]})}function St({isForUpdate:e}){const l=xe(ve),i=Le();if(e&&!l)return f.exports.useEffect(()=>{i("/personas")},[]),null;const c=Pe({mode:"all",defaultValues:e&&l?{...l,relBen:l.relBen.map(n=>({value:n})),photo:null}:void 0}),{formState:{errors:t,isValid:v},register:T,unregister:r,control:h,handleSubmit:j,watch:y,setValue:C}=c,{fields:O,append:D,remove:I}=Be({control:h,name:"relBen"}),_=y("relBen"),M=T("photo",{required:!e}),d=f.exports.useRef(null),u=n=>{M.ref(n),d.current=n},[o,$]=f.exports.useState(e?{mother:!!(l!=null&&l.mother),father:!!(l!=null&&l.father)}:{mother:!0,father:!0}),E=f.exports.useMemo(()=>!o.mother&&!o.father,[o]),x=y("photo"),[w,V]=f.exports.useState(e?l.attire.sweaterSize?0:1:0),G=f.exports.useMemo(()=>{const n=x==null?void 0:x[0];return n?URL.createObjectURL(n):l&&e?Fe(l):null},[x]),[J,F]=f.exports.useState(null),[U,Q]=f.exports.useState(null),[L,k]=f.exports.useState(null),A=y("information.birthdate"),q=f.exports.useMemo(()=>!U||U.length==0,[U]),[H,P]=f.exports.useState(!1),Z=n=>{P(!0);let s;e?s=Ve(n,l).then(()=>i("/personas")):s=De(n).then(p=>p.ok).then(p=>{p?i("/personas"):window.alert("No se pudo crear a la persona")}),s.catch(()=>window.alert("Ocurri\xF3 un error al intentar crear a la persona, intente de nuevo m\xE1s tarde")).then(()=>P(!1))};return f.exports.useEffect(()=>{(o.mother||o.father)&&(C("nonParent",null),r("nonParent")),o.mother||(C("mother",null),r("mother")),o.father||(C("father",null),r("father"))},[o]),f.exports.useEffect(()=>{w==0?(C("attire.dressSize",null),r("attire.dressSize")):(C("attire.sweaterSize",null),r("attire.sweaterSize"))},[w]),f.exports.useEffect(()=>{!!A&&A!=""&&Ge(A).then(n=>(n.length>0&&C("houseId",n[0].id,{shouldValidate:!0}),n)).then(Q)},[A]),f.exports.useEffect(()=>{qe().then(F)},[]),f.exports.useEffect(()=>{Oe().then(n=>l?n.filter(s=>s.childId!=l.id):n).then(k)},[]),a("form",{onSubmit:j(Z),children:z(Te,{...c,children:[z(b,{container:!0,spacing:3,children:[a(b,{item:!0,xs:12,children:a(Y,{sx:{color:"rgba(0,0,0,0.4)"},children:"INFORMACI\xD3N DEL BENEFICIARIO"})}),a(b,{item:!0,xs:1}),z(b,{item:!0,xs:10,children:[z(He,{onClick:()=>{var n;return(n=d.current)==null?void 0:n.click()},sx:{width:"100%",paddingTop:"56.25%",position:"relative",backgroundColor:G?"":"#545454",cursor:"pointer"},children:[G&&a("img",{alt:"Imagen del beneficiario",src:G,style:{width:"100%",height:"100%",cursor:"pointer",position:"absolute",inset:0}}),!G&&a("div",{style:{margin:"auto",position:"absolute",inset:0,display:"flex"},children:a("h2",{style:{margin:"auto",color:"white"},children:"Subir imagen del beneficiario"})})]}),a("label",{style:{display:"none"},htmlFor:"house-image",children:a("input",{accept:"image/*",type:"file",style:{display:"none"},...M,ref:u})})]}),a(b,{item:!0,xs:1}),a(b,{item:!0,xs:4,children:z(re,{fullWidth:!0,children:[a(le,{id:"gender-label",children:"Sexo"}),z(ie,{defaultValue:w,onChange:n=>V(n.target.value),labelId:"gender-label",label:"Sexo",children:[a(ee,{value:0,children:"Ni\xF1o"}),a(ee,{value:1,children:"Ni\xF1a"})]})]})}),a(ae,{isForUpdate:e,personalInformation:{},noAuto:!0,firstColumnWidth:8,control:h,fieldName:"information",validateCiUniqueness:!0}),a(b,{item:!0,xs:6,children:a(W,{control:h,name:"attire.shortOrTrousersSize",rules:{required:!0,min:1},defaultValue:0,render:({field:n})=>{var s,p,S,g,m;return a(N,{fullWidth:!0,error:!!((s=t==null?void 0:t.attire)!=null&&s.shortOrTrousersSize),variant:"outlined",label:"Talla de Short o Pantalones",type:"number",helperText:((S=(p=t==null?void 0:t.attire)==null?void 0:p.shortOrTrousersSize)==null?void 0:S.type)=="required"?"La Talla de Short o Pantalones no puede estar vac\xEDa":((m=(g=t==null?void 0:t.attire)==null?void 0:g.shortOrTrousersSize)==null?void 0:m.type)=="min"?"La Talla de Short o Pantalones no puede ser menor que 1":"",...n})}})}),a(b,{item:!0,xs:6,children:a(W,{control:h,name:"attire.tshirtOrshirtSize",defaultValue:0,rules:{required:!0,min:1},render:({field:n})=>{var s,p,S,g,m;return a(N,{fullWidth:!0,error:!!((s=t==null?void 0:t.attire)!=null&&s.tshirtOrshirtSize),variant:"outlined",label:"Talla de Camisa o Camiseta",type:"number",helperText:((S=(p=t==null?void 0:t.attire)==null?void 0:p.tshirtOrshirtSize)==null?void 0:S.type)=="required"?"La Talla de Camisa o Camiseta no puede estar vac\xEDa":((m=(g=t==null?void 0:t.attire)==null?void 0:g.shortOrTrousersSize)==null?void 0:m.type)=="min"?"La Talla de Camisa o Camiseta no puede ser menor que 1":"",...n})}})}),w==0&&a(b,{item:!0,xs:6,children:a(W,{control:h,name:"attire.sweaterSize",defaultValue:0,rules:{required:!0,min:1},render:({field:n})=>{var s,p,S,g,m;return a(N,{fullWidth:!0,error:!!((s=t==null?void 0:t.attire)!=null&&s.sweaterSize),variant:"outlined",label:"Talla de Sueter",type:"number",helperText:((S=(p=t==null?void 0:t.attire)==null?void 0:p.sweaterSize)==null?void 0:S.type)=="required"?"La Talla de Sueter no puede estar vac\xEDa":((m=(g=t==null?void 0:t.attire)==null?void 0:g.sweaterSize)==null?void 0:m.type)=="min"?"La Talla de Sueter no puede ser menor que 1":"",...n})}})}),w==1&&a(b,{item:!0,xs:6,children:a(W,{control:h,name:"attire.dressSize",defaultValue:0,rules:{required:!0,min:1},render:({field:n})=>{var s,p,S,g,m;return a(N,{fullWidth:!0,error:!!((s=t==null?void 0:t.attire)!=null&&s.dressSize),variant:"outlined",label:"Talla de Vestido",type:"number",helperText:((S=(p=t==null?void 0:t.attire)==null?void 0:p.dressSize)==null?void 0:S.type)=="required"?"La Talla de Vestido no puede estar vac\xEDa":((m=(g=t==null?void 0:t.attire)==null?void 0:g.sweaterSize)==null?void 0:m.type)=="min"?"La Talla de Vestido no puede ser menor que 1":"",...n})}})}),a(b,{item:!0,xs:6,children:a(W,{control:h,name:"attire.footwearSize",defaultValue:0,rules:{required:!0,min:1},render:({field:n})=>{var s,p,S,g,m;return a(N,{fullWidth:!0,error:!!((s=t==null?void 0:t.attire)!=null&&s.footwearSize),variant:"outlined",label:"Talla de Calzado",type:"number",helperText:((S=(p=t==null?void 0:t.attire)==null?void 0:p.footwearSize)==null?void 0:S.type)=="required"?"La Talla de Calzado no puede estar vac\xEDa":((m=(g=t==null?void 0:t.attire)==null?void 0:g.footwearSize)==null?void 0:m.type)=="min"?"La Talla de Calzado no puede ser menor que 1":"",...n})}})}),a(b,{item:!0,xs:12,children:a(W,{control:h,name:"houseId",rules:{required:!0},render:({field:n})=>z(re,{fullWidth:!0,children:[a(le,{id:"house-label",children:"Casa hogar a la que pertenece"}),a(ie,{labelId:"house-label",label:"Casa hogar a la que pertenece",disabled:q,...n,value:q?"unavailable":n.value,children:!q&&U.map(({id:s,name:p,rif:S})=>z(ee,{value:s,children:[p," (J-",S,")"]},s))})]})})}),a(b,{item:!0,xs:12,children:a(Y,{sx:{color:"rgba(0,0,0,0.4)"},children:a(ne,{control:a(oe,{onChange:n=>$({...o,mother:n.target.checked}),checked:o.mother}),label:"MADRE"})})}),o.mother&&a(ae,{isForUpdate:e,personalInformation:J,control:h,fieldName:"mother"}),a(b,{item:!0,xs:12,children:a(Y,{sx:{color:"rgba(0,0,0,0.4)"},children:a(ne,{control:a(oe,{onChange:n=>$({...o,father:n.target.checked}),checked:o.father}),label:"PADRE"})})}),o.father&&a(ae,{isForUpdate:e,personalInformation:J,control:h,fieldName:"father"}),a(b,{item:!0,xs:12,children:a(Y,{sx:{color:"rgba(0,0,0,0.4)"},children:a(ne,{control:a(oe,{checked:E}),label:"TUTOR"})})}),E&&a(ae,{isForUpdate:e,personalInformation:J,control:h,fieldName:"nonParent"}),a(b,{item:!0,xs:12,children:a(Y,{sx:{color:"rgba(0,0,0,0.4)"},children:"BENEFICIARIOS RELACIONADOS"})}),L&&O.map((n,s)=>{const p=L.find(({childId:g})=>g==_[s].value),S=p?[p,...L.filter(({childId:g})=>!_.find(m=>m.value==g))]:L.filter(({childId:g})=>!_.find(m=>m.value==g));return z(Re.Fragment,{children:[a(b,{item:!0,xs:11,children:z(re,{fullWidth:!0,children:[a(le,{id:`rel-ben-${s}`,children:"Seleccionar beneficiario relacionado"}),a(W,{control:h,defaultValue:S[0].childId,name:`relBen.${s}.value`,rules:{required:!0},render:({field:g})=>a(ie,{labelId:`rel-ben-${s}`,label:"Seleccionar beneficiario relacionado",...g,children:S.map(m=>z(ee,{value:m.childId,children:[m.information.name," ",m.information.lastname]},m.childId))})})]})}),a(b,{item:!0,xs:1,children:a(be,{variant:"contained",color:"error",sx:{py:1.4,mr:"auto"},onClick:()=>I(s),children:a(je,{fontSize:"large"})})})]},s)}),L&&_.length<L.length&&a(b,{item:!0,xs:12,children:a(be,{fullWidth:!0,color:"secondary",onClick:()=>D({value:""}),children:a(Ne,{fontSize:"large"})})})]}),a(_e,{disabled:!v||Object.keys(t).length>0,loading:H,type:"submit",fullWidth:!0,variant:"contained",size:"large",sx:{mt:4},children:"Guardar Beneficiario"})]})})}export{St as default};
