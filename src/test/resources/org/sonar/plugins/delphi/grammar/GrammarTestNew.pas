unit GrammarTestNew;

interface

uses Types, SysUtils, System.Classes, System.Generics.Collections , System.Rtti;

Type
  TMyChar = Char;
  TMySetOfChar = set of Char;
  
  TPageControl = class(ComCtrls.TPageControl)
  private
    procedure TCMAdjustRect(var Msg: TMessage); message TCM_ADJUSTRECT;
  end;  

  TClassA = class;
  IInterfaceA = interface;

  AnnotationAttribute = class(TCustomAttribute)
  end;

  TAbstractClass = class abstract
  end;

  TSealedClass = class sealed
  end;

  ILoggerFactory = interface
    ['{1CDEE569-3E91-4A5D-8173-F21305DB8BF2}']
  end;

  TEmptyLoggerFactory = class(TInterfacedObject, ILoggerFactory, IUnknown, IInvokable)
  end;

  TLoggerFactory = class(TInterfacedObject, ILoggerFactory, IUnknown, IInvokable)
  strict private
    class var Instance: ILoggerFactory;
  strict
  private
    class constructor Create;
    procedure value(strict: integer);//Keyword strict
  end;

  [Annotation]
  [Annotation()]
  TStaticClass = class
  private
    [Annotation] FValue: Integer;
  public
    [Annotation] class function StringToStringDynArray(Values: Array of String): TStringDynArray;
    [Annotation] class procedure ProcStringToStringDynArray(Values: Array of String);
    [Annotation] function StringToStringDynArray2(Values: Array of String): TStringDynArray;
    [Annotation] procedure ProcStringToStringDynArray2(Values: Array of String);
  strict private
    FA: string;
  private
    class  var a: Double;
  strict private
    class var Instance: TObject;
  end;

  TEnumType = (eUm, eDois);
  TSetOfEnumType = set of TEnumType;

  ///	<summary>
  ///	  Documentation Insight comment
  ///	</summary>
  IInterfaceA = interface

  end;

  IGenericA<T> = interface(IInterfaceA)
    procedure GenProc(Value: T);
  end;

  GenericA<T> = record
  strict private
    FValue: IGenericA<T>;
  private
    function GetValue: IGenericA<T>;
  public
    property Value: IGenericA<T>  read GetValue;

    class operator Implicit(const value: GenericA<T>): IGenericA<T>;
  end;

  {$M+}
  TClassA = class(TInterfacedObject)
  strict private
    FStrictPrivate: String;
  strict protected
    FStrictProtected: Double;
  private
    FPrivate: Integer;
    FCode: Integer;
    procedure SetCode(const Value: Integer);
  protected
    FProtected: TDateTime;
  public
    function DoStuff: string;
    procedure DoSomeStuff;

    procedure EmptyBlocks;
    procedure Arrays;
  published
    property Code: Integer read FCode write SetCode;
  end;
  {$M-}

  TGenericA<T> = class(TClassA, IGenericA<T>)
  private
    FOnIdle: TProc<T>;
  public
    procedure AfterConstruction; override;

    property OnIdle: TProc<T> read FOnIdle write FOnIdle;

    function DoMoreStuff: T;

    procedure GenProc(Value: T);
  end;

  TOtherGeneric<T, TResult> = class(TGenericA<T>)
  private
    FOnIdle2: TFunc<T, TResult>;
  public
    procedure AfterConstruction; override;
  published
    property OnIdle2: TFunc<T,TResult> read FOnIdle2 write FOnIdle2;
  end;

  TRestrictGenericA<T : Class, Constructor> = class(TGenericA<T>)
  private
  public
    function DoNothing<TResult>(Value: T): T;
    procedure ProcDoNothing<TResult>(Value: T);deprecated;
  end;

  TClassType<T> = class
  end;

  TClassType<T,R> = class
  end;
  
  TMyClass = class

  end;

  TMetaClassClass = class of TMyClass;

  TOuterClass = class
  strict private
    type
      TInnerClass = class
        constructor Create(AOwner: TComponent);
      end;
  end;

  TSortOrder<T> = class
  strict private
    type
      TSortItem<T> = class
        Value: T;
        constructor Create(AValue: T);
        function GetValue: T;
      end;
      TRecordItem = record
        class operator LogicalAnd(const left, right: TRecordItem): TRecordItem;
      end;
  private
    FOrders: TList<TSortItem<T>>;
  end;

(* AVICAP.DLL *)
 function capCreateCaptureWindowA (
  	      lpszWindowName  : PChar;
 			    dwStyle         : DWord;
 	  		  x, y            : Integer;
          nWidth, nHeight : Integer;
 		    	hwndParent      : THandle;
     		  nID             : Integer ) : THandle; stdcall; external 'avicap32.dll';

implementation


{ TClassA }

procedure TClassA.Arrays;
var
  intArray: array[0..3] of Integer;
  vSet: TSetOfEnumType;
begin
  vSet := []; //Empty set
  vSet := [eUm];

  Include(vSet, eDois);
  Exclude(vSet, eUm);
end;

procedure TClassA.DoSomeStuff;
type
  MyDouble = type Double;
var
  read: MyDouble;
begin
  read := 1 + 2 * 3 / 4;
end;

function TClassA.DoStuff: string;
var
  a: IGenericA<Integer>;
begin
  Code := 987654321;

  a := TGenericA<Integer>.Create;
  a.GenProc(01234);
end;

procedure TClassA.EmptyBlocks;
var
  i: Integer;
  MyElem: Integer;
  MyList: TList<Integer>;
  a: Integer;
begin
  while True do;

  while True do
  begin
  end;

  repeat
  until (True);

  for i := 0 to 0 do;

  for i := 0 to 0 do
  begin
  end;

  for MyElem in MyList do
  begin
  end;

  try
  finally
  end;

  try
  except
      on e: EAbort do;
  end;

  try
  except
      //on EAbort do;
      {TODO -oFabricio -cGrammar : H? um problema na gram?tica, tem que haver um espa?o
            antes do : (dois pontos) }
      on EAbstractError do;
      on e: EAbort do;
      on e : Exception do;
  end;

  with Self do;

  with Self do
  begin

  end;
end;

procedure TClassA.SetCode(const Value: Integer);
begin
  FCode := Value;
  FStrictPrivate := IntToStr(FCode);
  FPrivate := FCode;
end;

{ TGenericA<T> }

procedure TGenericA<T>.AfterConstruction;
begin
  inherited;
  FOnIdle := procedure (Value: T)
             begin
               Writeln('Default OnIdle event');
             end;
end;

function TGenericA<T>.DoMoreStuff: T;
begin

end;

procedure TGenericA<T>.GenProc(Value: T);
begin

end;

{ TRestrictGenericA<T> }

function TRestrictGenericA<T>.DoNothing<TResult>(Value: T): T;
begin
  //Exit with parameter
  Exit(Value);
end;

procedure TRestrictGenericA<T>.ProcDoNothing<TResult>(Value: T);
begin
  raise Exception.Create('Error Message');
end;

{ GenericA<T> }

function GenericA<T>.GetValue: IGenericA<T>;
begin
  Result := FValue;
end;

class operator GenericA<T>.Implicit(const value: GenericA<T>): IGenericA<T>;
begin
  Result:=value.Value;
end;

{ TOtherGeneric<T, TResult> }

procedure TOtherGeneric<T, TResult>.AfterConstruction;
begin
  inherited;
  FOnIdle2 := function (Value: T): TResult
              begin
                Writeln('Default OnIdle2 event' );
                Result := Default(TResult);
              end;
end;

{ TStaticClass }

class procedure TStaticClass.ProcStringToStringDynArray(Values: array of String);
begin

end;

procedure TStaticClass.ProcStringToStringDynArray2(Values: array of String);
begin

end;

class function TStaticClass.StringToStringDynArray(Values: array of String): TStringDynArray;
begin

end;

function TStaticClass.StringToStringDynArray2(Values: array of String): TStringDynArray;
begin

end;

{ TLoggerFactory }

//class constructor TLoggerFactory.Create;
//begin
//  GlobalContainer.RegisterType<TLoggerFactory>
//                 .Implements<ILoggerFactory>;
//end;

{ TLoggerFactory }

class constructor TLoggerFactory.Create;
begin

end;

{ TOuterClass.TInnerClass }

constructor TOuterClass.TInnerClass.Create(AOwner: TComponent);
begin
end;

{ TSortOrder<T>.TSortItem<T> }

function TSortOrder<T>.TSortItem<T>.GetValue: T;
begin

end;

constructor TSortOrder<T>.TSortItem<T>.Create(AValue: T);
begin

end;

{ TSortOrder<T>.TRecordItem }

class operator TSortOrder<T>.TRecordItem.LogicalAnd(const left, right: TRecordItem): TRecordItem;
var
  vImmutable: Integer;
begin
  vImmutable := (high(Integer))-1;
end;

type
  TCustomer = Record
    name : string[20];
    age  : Byte;
  end;

var
  custRecPtr : ^TCustomer;
  
procedure CustomerCreate;
begin
  // Create a customer record using 'New'
  New(custRecptr);
  try
	  // Assign values to it
	  custRecPtr.name := 'Her indoors';
	  custRecPtr.age  := 55;
	
	  // Now display these values
	  ShowMessageFmt('%s is %d',[custRecPtr.name, custRecPtr.age]);
	
	  // Now dispose of this allocated record
  finally
    Dispose(custRecPtr);
  end;  
end;  

initialization
//  GlobalContainer.RegisterType<TLoggerFactory>
//                 .Implements<ILoggerFactory>;
//                 .AsSingleton;
finalization

end.
